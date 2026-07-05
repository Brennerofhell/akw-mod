# Changelog

Alle nennenswerten Änderungen an der AKW-Mod werden hier dokumentiert.

Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
Versionierung nach [Semantic Versioning](https://semver.org/lang/de/).

## [1.3.0] — Unveröffentlicht

Multiblock-Phasen A–C: Energie- und Item-Ports, rechteckige Hüllen 3–9 je Achse, präzise
Fehlerliste, Tab-GUI mit Steuerstab-Regler und Schichtansicht, Zustandsautomat mit
Nachzerfallswärme und reparierbaren beschädigten Kernen.

### ⚠️ Breaking Changes
- **FE-Abgabe nur noch über Energie-Ports:** Der Multiblock-Controller besitzt **keine
  Energie-Capability mehr** und gibt selbst kein FE mehr ab. Jede Hülle braucht mindestens
  einen **Reaktor-Energie-Port** (`akw:reactor_energy_port`), sonst schlägt die Validierung
  mit `akw.reactor.error.no_energy_port` fehl. **Bestehende Reaktoren ohne Energie-Port
  disassemblieren nach dem Update binnen ~5 s** (Revalidierung alle 100 Ticks) — Port in die
  Hülle einsetzen und mit dem Schraubenschlüssel neu assemblieren.
- **Hopper am Controller funktionieren nicht mehr:** Brennstoffzufuhr und Abfallentnahme
  laufen ausschließlich über den **Reaktor-Item-Port** (`akw:reactor_item_port`).
- Sprach-Keys `akw.multiblock.error.*` entfernt (ersetzt durch `akw.reactor.error.*`);
  zusätzlich der tote Key `akw.multiblock.need_wrench` entfernt (das GUI öffnet seit
  Phase B auch unassembliert, siehe unten).

### Hinzugefügt (Phase A — Ports & Hülle)
- **Reaktor-Energie-Port** (`akw:reactor_energy_port`): einziger FE-Abgabepunkt der Hülle.
  Hält keinen eigenen Speicher, sondern delegiert an den Controller und schiebt pro Tick
  aktiv bis 32 768 FE je Seite an angrenzende Verbraucher. Rezept: **Energie-Kabel über
  Reaktor-Gehäuse** (shaped, 1×2 senkrecht).
- **Reaktor-Item-Port** (`akw:reactor_item_port`): Hopper-Anschluss des Multiblocks mit
  BlockState-Property `mode` (*fuel_input* / *waste_output* / *disabled*); **Rechtsklick ohne
  Schraubenschlüssel** schaltet den Modus um (Actionbar-Meldung). Delegiert je nach Modus auf
  den Brennstoff- bzw. Abfall-Slot des Controllers. Rezept: **Trichter über Reaktor-Gehäuse**.
- **Rechteckige Reaktorhüllen 3–9 Blöcke je Achse** (bis 9×9×9): Erkennung per BFS ab der
  Controller-Position; der Controller darf an **beliebiger Hüllenposition** sitzen
  (Wand, Kante oder Ecke).
- **Präzise Fehlerliste**: `ValidationError`-Typen `GAP`, `FOREIGN_BLOCK`, `NO_CORE`,
  `NO_ENERGY_PORT`, `TOO_LARGE`, `DISCONNECTED_PIPE` (letzterer nicht-blockierende Warnung);
  max. 8 Fehler pro Validierung. Der Wrench-Klick zeigt `akw.multiblock.invalid` in der
  Actionbar und alle Fehler als **Chat-Zeilen mit Koordinaten** (`akw.reactor.error.*`).

### Technisch (Phase A — Ports & Hülle)
- `ReactorValidator` neu: `find()` (BFS) + `validateBounds()` — die Tick-Revalidierung alle
  100 Ticks läuft über die **gespeicherten Grenzen** (kein `facing`-Parameter mehr).
  Konstanten `MAX_EDGE=9`, `MAX_VOLUME=729`, `MAX_ERRORS=8`.
- `ReactorLayout`: `relMinX/Y/Z` + `sizeX/Y/Z` + `energyPortCount`/`itemPortCount` statt
  `outerSize`; `maxDimension()` (u. a. Explosionsstärke `4 + maxDimension`).
- Controller-NBT: neue Keys `RelMinX/Y/Z`, `SizeX/Y/Z`, `EnergyPortCount`, `ItemPortCount`;
  **Migration** vom alten zentrierten `ReactorSize`-Feld beim Laden (Umrechnung über das
  Blockstate-`FACING`).
- Neue BlockEntities `akw:reactor_energy_port` / `akw:reactor_item_port` ohne eigenen
  Speicher/Inventar; NBT-Key `Controller` (Long, Sentinel `Long.MIN_VALUE`); Verlinkung
  ausschließlich durch den Controller (Assemble / erfolgreiche Revalidierung / Disassemble,
  `updatePortLinks`). Energie-Capability des Ports liefert `resolveControllerEnergy()`
  (nur bei `ASSEMBLED`).
- Bauroboter: `finish()` validiert per `ReactorValidator.find()`; ein fehlender Energie-Port
  zählt **nicht** als Baufehler — der Roboter baut weiterhin nur die 3×3×3-Casing-Hülle,
  Ports rüstet der Spieler nach (bekannte Einschränkung).
- Datagen: Modelle, Blockstates, Loot-Tabellen, `mineable/pickaxe`-Tag, Rezepte und
  Sprache (de/en) für beide Ports; Ports im Creative-Tab und in `FUNCTIONAL_BLOCKS`.

### Hinzugefügt (Phase B — Multiblock-GUI)
- **Eigenständiges Tab-GUI** für den Multiblock-Controller (`ModularReactorScreenHandler` /
  `ModularReactorScreen`, 176×222, eigene Textur `textures/gui/modular_reactor.png`) statt
  des bisher geteilten `NuclearReactorScreen`; die MenuType-ID `multiblock_reactor` bleibt
  unverändert. Drei Reiter:
  - **Übersicht** — Hüllengröße, Kernzahl, FE-Füllstand, Erzeugung, Kühlung, Hitze, Status.
  - **Steuerung** — Redstone-/Komparator-Modus, Ein/Aus-Schalter, einstellbare
    **Abschalttemperatur (50–95 %)**, „Sicherung überbrücken"-Schalter und ein stufenloser
    **Steuerstab-Regler (0–100 %)**.
  - **Diagnose** — Fehlerliste (bisher nur im Chat) sowie eine **Schichtansicht** des
    Innenraums mit Farbcodes (Kern/Steuerstab/Kühlrohr/Blei/Fremdblock) und
    Y-Schicht-Pfeiltasten.
- **GUI öffnet jetzt auch unassembliert** und zeigt dann direkt den Diagnose-Tab mit der
  Fehlerliste, statt nur eine Chat-Meldung anzuzeigen.
- **Stufenloser Steuerstab-Regler:** `ReactorSimulation.calculate()` um den Parameter
  `controlRodInsertion` erweitert — `Endreaktivität = Grundreaktivität × (1 − Einschub/100)`.
  Bei 100 % Einschub produziert der Reaktor keine Energie und keine Wärme mehr.

### Hinzugefügt (Phase C — Zustandsautomat & Reaktorsicherheit)
- **Zustandsautomat `reactor/ReactorStatus`:** `UNASSEMBLED`, `OFFLINE`, `STARTING`,
  `RUNNING`, `SCRAM`, `COOLDOWN`, `DAMAGED` ersetzen die bisherigen Ad-hoc-Flags im
  Controller-Tick.
- **Nachzerfallswärme:** nach einer Abschaltung (Auto-SCRAM bei der einstellbaren
  Abschalttemperatur, GUI-Ausschalter oder Not-Aus-Redstone-Modus) gibt der Reaktor über
  `DECAY_TICKS=200` linear abklingend noch **20 % der letzten Kernwärme** ab, bevor er in
  `COOLDOWN` und schließlich (`heat < 5 % Maximalhitze`) in `OFFLINE` übergeht.
- **Beschädigte Kerne statt sofortiger Explosion:** bei 100 % Maximalhitze werden **1–3
  zufällige Kerne** zu `akw:damaged_reactor_core` (neuer Block, produziert nichts, im
  Validator ein gültiger, inerter Innenraumblock). Reparatur per
  **Schraubenschlüssel-Rechtsklick**, sobald der Reaktor abgekühlt ist
  (`isTooHotForRepair()`). Eine echte Explosion passiert nur noch, wenn im Steuerungs-Tab
  die **Sicherung überbrückt** wurde, oder wenn die Hülle bei ≥75 % Maximalhitze zerstört
  wird.

### Technisch (Phase B/C)
- `MultiblockReactorControllerBlockEntity`: `ContainerData` auf **20 Properties** erweitert
  (`MB_PROPERTY_COUNT`); neue Indizes 8–19 für Kernzahl, Erzeugung, Kühlung, Steuerstab,
  Ein/Aus, Abschalttemperatur, Fehlerzahl, Größe X/Y/Z, Status, Sicherung. Rein abgeleitete
  Properties laufen über dedizierte Client-Spiegelfelder (siehe „Behoben").
- Fehlerliste und Innenraum-Schnitt (Schichtansicht) laufen über das
  **BlockEntity-Update-Tag** (`getUpdateTag`, Int-Arrays `ClientErrors` + `InteriorGrid`),
  nicht über `ContainerData` — zu groß für einzelne Properties.
- Neue NBT-Keys: `ControlRodInsertion`, `Enabled`, `ShutdownTemp`, `Status`, `StartupTimer`,
  `DecayHeatBase`, `DecayTicksLeft`, `SafetyOverride`.
- `ReactorValidator` behandelt `damaged_reactor_core` wie `LEAD_BLOCK` — gültiger, aber
  inerter Innenraumblock (zählt nicht als Kern).
- `ReactorSimulation.calculate()`: Kühlung wird jetzt **vor** der `activeCores`-Prüfung und
  unabhängig von ihr berechnet (skaliert mit der installierten Kernzahl statt mit
  `activeCores`) — sonst wäre die Kühlung während SCRAM/COOLDOWN/DAMAGED wirkungslos.
  `effectiveCapacity()` deckelt zusätzlich gegen `energyStorage.getMaxEnergyStored()`
  (20 Mio. FE), da die reine Formel bei sehr großen Hüllen (bis 9×9×9) bis 34,3 Mio. FE
  ergeben kann.
- Datagen: Blockstate, Modell, Loot-Tabelle, `mineable/pickaxe`-Tag und Sprache (de/en) für
  `damaged_reactor_core`; kein Rezept (entsteht nur durch Überhitzung, nicht craftbar).
- **Unit-Tests:** 52 JUnit-5-Tests unter `src/test/java/ch/danielt/akw/reactor/` für
  `ReactorSimulation`, `ReactorLayout`, `ValidationError`, `ItemPortMode`, `RedstoneMode`,
  `ComparatorMode`, `ReactorStatus`. `build.gradle`: JUnit-5-BOM, `useJUnitPlatform()`,
  ModDevGradle-`unitTest { enable(); testedMod = mods.akw }`.

### Behoben
Adversarialer Review nach Phase B/C fand und behob folgende Fehler (alle noch vor
Veröffentlichung von v1.3.0):

1. **`DAMAGED` war ein Sackgassenzustand:** reparierte Kerne führten nie zurück zu
   `OFFLINE`; Strahlung lief auch nach vollständiger Abkühlung/Reparatur unbegrenzt weiter.
   Fix: Ausgangstransition bei der 100-Tick-Revalidierung (`hasDamagedCores()`-Scan),
   Strahlung an `isTooHotForRepair()` gekoppelt.
2. **Kühlung fiel während SCRAM/COOLDOWN/DAMAGED auf den Passivwert zurück**, weil
   `ReactorSimulation.calculate()` die Kühlrohr-Kontakte an `activeCores` koppelte (bei
   SCRAM = 0) — die Nachzerfallswärme konnte dadurch trotz vorhandener Kühlrohre
   zuverlässig die Schadensschwelle überschreiten, und die Schutzabschaltung beschädigte
   so die Kerne, die sie eigentlich schützen sollte. Fix: Kühlung skaliert jetzt mit der
   vollen installierten Kernzahl, unabhängig vom Brennzustand.
3. **Alle Kerne beschädigt (z. B. jeder 1-Kern-Minimalreaktor):** der Validator meldete
   `NO_CORE` trotz intakter Hülle, wodurch der Controller still disassemblierte statt das
   Reparatur-Fenster offen zu halten. Fix: Sonderfall in der Revalidierung, wenn
   ausschließlich `NO_CORE` während `DAMAGED` gemeldet wird.
4. **NBT-Migration:** alte Stände (v1.2.0/Phase A/B, kein `Status`-Key) mit laufendem
   Brennzyklus wurden zu `OFFLINE` migriert, obwohl `burnTime`/`activeCores` noch liefen —
   das GUI zeigte inkonsistente Werte. Fix: impliziter `RUNNING`-Status bei
   `burnTime>0 && activeCores>0`.
5. **Client-GUI zeigte veraltete Status-/Produktions-/Kühlungswerte:**
   `DataSlot.forContainer(...)` leitet den Server→Client-Sync direkt an
   `ContainerData.set(index, value)` auf dem Client weiter (bytecode-verifiziert); für rein
   abgeleitete Properties (Status, Kernzahl, Erzeugung, Kühlung, Fehlerzahl, Größe X/Y/Z,
   Kapazität, Maximalhitze) landete das im `default`-Zweig und wurde verworfen. Fix:
   dedizierte Client-Spiegelfelder.
6. **Hitze beim Laden unclamped:** konnte über der Schadensschwelle geladen werden und
   sofort `damageCores()`/`explode()` im ersten Tick auslösen. Fix: Clamp auf
   `effectiveMaxHeat()-1` nach dem Laden.
7. **Steuerstab-Regler-Fokus-Desync:** der Regler übernahm Server-Werte nicht mehr, sobald
   er (auch ohne Ziehen) Tastaturfokus hatte — im Mehrspieler-Betrieb potenziell eine
   dauerhafte Anzeigeabweichung. Fix: nur noch an `dragging` gekoppelt.
8. **Kapazität konnte die reale Speichergrenze übersteigen:** `ReactorSimulation.capacity()`
   kann bei sehr großen Hüllen (bis 9×9×9) rechnerisch bis 34,3 Mio. FE ergeben, der reale
   `energyStorage` ist aber hart auf 20 Mio. FE begrenzt — der Energiebalken wurde nie voll
   und der Reaktor verbrannte unnötig weiter Brennstoff. Fix: `effectiveCapacity()` deckelt
   jetzt gegen `energyStorage.getMaxEnergyStored()`.
9. Toter Sprach-Key `akw.multiblock.need_wrench` entfernt (de + en).

### Bekannte Probleme
- **ContainerData-Sync auf 16 Bit begrenzt** (nicht in dieser Version behoben):
  `ClientboundContainerSetDataPacket` überträgt jeden Wert per
  `FriendlyByteBuf.writeShort`/`readShort` — Werte über 32 767 werden truncated bzw. als
  negative Zahl angezeigt. Betrifft potenziell **alle** Reaktor-GUIs (Energie, Kapazität,
  bei großen Multiblocks auch Hitze), nicht nur die Phase-B/C-Änderungen. Geplante
  Behebung (Low/High-Word-Splitting) siehe TODO.md „Tests und Stabilität".

---

## [1.2.0] — 2026-06-29

### Hinzugefügt
- **Bauroboter** (`reactor_builder_controller`): baut einen 3×3×3-Multiblock-Reaktor
  vollautomatisch aus seinem 27-Slot-Inventar (Reaktor-Gehäuse, Reaktorkern, Multiblock-Controller)
  + Energie (**500 FE pro gesetztem Block**). Ein sichtbarer Roboter (ArmorStand) setzt alle
  5 Ticks einen Block; Shift-Rechtsklick startet/pausiert, Rechtsklick öffnet das Inventar.
  Status (fehlendes Material / Blockade / zu wenig Energie / fertig) erscheint in der Actionbar,
  der Komparator-Ausgang zeigt den Baufortschritt 0–15, ein Redstone-Signal pausiert den Bau.
  Ist die Struktur fertig und gültig, ist der Multiblock-Reaktor sofort assembliert.
- **Konfigurierbare Redstone-Modi (4)** im Reaktor-GUI (Einblock- **und** Multiblock-Reaktor),
  per Button umschaltbar: *Ignoriert*, *Signal aktiviert*, *Signal deaktiviert* (Standard),
  *Not-Aus (SCRAM)*. SCRAM stoppt den laufenden Brennstab sofort statt ihn abbrennen zu lassen.
- **Konfigurierbare Komparator-Modi (4)**: *Energie* (Standard), *Temperatur*, *Brennstoff*,
  *Abfall* — bestimmen, welchen Füllstand der Komparator als Signal 0–15 ausgibt.

### Technisch
- Neue Enums `reactor/RedstoneMode` und `reactor/ComparatorMode` (je `next()` + Translation-Keys
  `akw.redstone_mode.*` / `akw.comparator_mode.*`).
- `ContainerData` um `IDX_REDSTONE_MODE` und `IDX_COMPARATOR_MODE` erweitert (`PROPERTY_COUNT` = 8);
  beide Modi werden per NBT (`RedstoneMode`, `ComparatorMode`) persistiert.
- Zwei GUI-Buttons in `NuclearReactorScreen` + `clickMenuButton`-Handler (id 0 = Redstone,
  id 1 = Komparator); Multiblock teilt sich Screen und Handler.
- Neuer BlockEntity-Typ `akw:reactor_builder_controller` mit registrierter Energie-Capability
  (`Capabilities.Energy.BLOCK`); Block mit `FACING`/`ACTIVE`/`POWERED`-State.

### Dokumentation
- Neu: **`docs/REFERENCE.md`** — vollständige, klassengenaue technische Referenz (jede Klasse,
  Konstante, Formel, Blockstate, NBT-Key, Registry-ID, GUI-Layout, Datagen, Worldgen).
- `README.md`, `docs/GUIDE.md`, `docs/ARCHITECTURE.md` und `CLAUDE.md` aktualisiert
  (Bauroboter, Redstone-/Komparator-Modi ergänzt; veraltete „Team Reborn Energy"-/
  `EnergyStorage.SIDED`-Hinweise entfernt — der Mod nutzt NeoForge-natives FE).

---

## [1.1.0] — 2026-06-22

### Geändert — Port von Fabric auf NeoForge

Vollständige Migration von **Fabric (Yarn-Mappings, Team Reborn Energy)** auf **NeoForge 21.10.64**
(Minecraft 1.21.10, Mojang-Mappings). Gameplay unverändert. Details: [docs/NEOFORGE-MIGRATION.md](docs/NEOFORGE-MIGRATION.md).

- **Build/Metadaten:** `fabric-loom` → `net.neoforged.moddev`; `fabric.mod.json` → `META-INF/neoforge.mods.toml`;
  Einstieg über `@Mod`-Klasse + `@EventBusSubscriber`. Jar-Ausgabe nach `build/libs/neoforge/`.
- **Energie:** Team Reborn Energy entfernt → NeoForge-native Transfer-API
  (`Capabilities.Energy` / `EnergyHandler` / `SimpleEnergyHandler` + Transaktionen). Neuer Speicher
  `MutableEnergyStorage`, FE-Verteilung über `EnergyNet` mit `EnergyHandlerUtil.move`.
- **Registrierung:** `DeferredRegister`-Muster; `BlockEntityType` über neuen Konstruktor;
  BlockItems via `registerSimpleBlockItem` (behebt „Item id not set" beim Laden).
- **Block-Lifecycle (1.21.10):** `getAnalogOutputSignal(…, Direction)`; `onRemove` → automatisches
  Container-Dropping via `preRemoveSideEffects`.
- **GUI:** RenderPipeline-basiertes `blit`, `setTooltipForNextFrame`.
- **Datagen:** Fabric-Datagen → NeoForge `GatherDataEvent` (Client/Server-Split); Provider für Sprache,
  Tags, Loot, Rezepte, Modelle und Advancements neu auf vanilla/NeoForge-Basisklassen. Assets in
  1.21.10-Struktur neu generiert.

---

## [1.0.0] — 2026-06-20

### Release
- Erster stabiler Release des Atomkraftwerk-Mods.
- Vollständiger Gameplay-Loop: Uranabbau → Anreicherung → Reaktor → Energie → Abfallmanagement.
- README vollständig überarbeitet: korrekte Feature-Liste, Reaktor-Werte, Installation, Projektstruktur.
- `fabric.mod.json`: GitHub-Links in `contact`-Feld eingetragen.
- ROADMAP Phase 5 abgeschlossen.

---

## [0.9.5] — 2026-06-20

### Hinzugefügt
- **Sound-Registrierung** (`ModSounds`): drei Sound-Events registriert —
  `reactor_ambient`, `reactor_alert`, `reactor_meltdown`. Infrastruktur ist bereit;
  Sounds werden stumm gespielt bis `.ogg`-Dateien in `assets/akw/sounds/` abgelegt werden.
  `sounds.json` mit Subtitle-Verweisen angelegt.
- **Strahlungspartikel** (vanilla `ELECTRIC_SPARK`): Reaktoren (Standard + Multiblock)
  spawnen alle 10 Ticks grüne Funken-Partikel über dem Block, solange ein Brennstab brennt.
- **Abfallbehälter-Partikel** (vanilla `GLOW_SQUID_INK`): bei Füllstand > 50 %
  alle 20 Ticks Warnsignalpartikel.
- **Advancement-Kette** (9 Stufen via `ModAdvancementProvider`, Datagen):
  Uranabbau → Schmelzen → Anreicherung → Brennstab → Erster Reaktor → Energie online
  → (Elite-Reaktor / Fusionsreaktor / Multiblock-Meister als Challenges).
- Sound-Untertitel + Advancement-Übersetzungen in DE und EN.

---

## [0.8.0] — 2026-06-20

### Hinzugefügt
- **Komparator-Output** für alle Reaktor-Typen (Standard + Multiblock): Komparator
  liest den Energie-Füllstand als Signal 0–15 aus — 0 = leer, 15 = voll.
- **Redstone pausiert Reaktor** (neues Property `POWERED`): Redstone-Signal am
  Reaktor-Block stoppt das Zünden neuer Brennstäbe. Der laufende Stab brennt noch
  ab, danach geht der Reaktor sanft in Standby.
  Praxisnutzen: Komparator-Signal „voll" → Leitung → benachbarter Reaktor pausiert
  → automatische Lastverteilung.
- **Abfallbehälter** (`waste_container`) ist jetzt ein vollständiger Speicher-Block:
  - 9 Slots für Verbrauchte Brennstäbe (GUI öffnet sich per Rechtsklick)
  - Komparator-Output: Füllstand 0–15
  - Hopper von oben/seitig → Einlagern (nur Verbrauchte Brennstäbe)
  - Hopper von unten → Entnehmen
  - Passive Strahlung (Level 0) bei Füllstand > 50 % in 5-Block-Radius
    (Blei-Block auf dem Pfad schützt vollständig)
- Pfadbasierter Strahlungsschutz auch im **Multiblock-Reaktor** nachgezogen
  (war bisher nur im Standard-Reaktor implementiert).

### Technisch
- `NuclearReactorBlock` + `MultiblockReactorControllerBlock`: `hasComparatorOutput`,
  `getComparatorOutput`, `neighborUpdate` (POWERED-Property).
- `WasteContainerBlock` + `WasteContainerBlockEntity` neu (SidedInventory,
  NamedScreenHandlerFactory → GenericContainerScreenHandler).
- Blockstate-JSONs aller 6 Reaktortypen um `powered=false/true` erweitert.
- `NuclearReactorBlockEntity.hasLeadShielding` ist jetzt package-private
  (von WasteContainerBlockEntity wiederverwendet).

---

## Frühere Versionen (< 0.8.0)

Die ausführliche Historie der Versionen 0.1.0–0.7.0 wurde entrümpelt.
Kurzüberblick der wichtigsten Meilensteine vor 0.8.0:

- **0.7.0** — Verbrauchter Brennstab + Abfall-Slot im Reaktor-GUI, Hopper-Kompatibilität, pfadbasierte Strahlungsabschirmung.
- **0.6.0** — Multiblock-Reaktor-System (Gehäuse, Controller, Schraubenschlüssel; 3×3×3 / 5×5×5 / 7×7×7 mit skalierender Leistung).
- **0.5.x** — Steuerstab-Logik (Hitzereduktion), Strahlungs-Effekt mit Blei-Abschirmung, Angereichertes Uran als Brennstab-Zwischenprodukt.
- **0.3.x–0.4.x** — Energie-Infrastruktur (Kabel + Akku-Block), Kühlsystem/Hitze-Mechanik mit Überhitzungsexplosion, Upgrade auf MC 1.21.10, Kreativ-Tab.
- **0.1.x–0.2.0** — Grundgerüst (Registry, Items, Erze), Uran-Verarbeitungskette, 6 Reaktor-Typen als FE-Generatoren, Texturen und Uranerz-Weltgenerierung.
