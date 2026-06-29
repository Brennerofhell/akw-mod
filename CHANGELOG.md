# Changelog

Alle nennenswerten Änderungen an der AKW-Mod werden hier dokumentiert.

Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
Versionierung nach [Semantic Versioning](https://semver.org/lang/de/).

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
