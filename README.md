# Atomkraftwerk (AKW) — Minecraft NeoForge Mod

Baue dein eigenes Atomkraftwerk: Uran abbauen, anreichern, Brennstäbe herstellen,
Reaktor betreiben und FE-kompatiblen Strom erzeugen. Mit Radioaktivitätsmechanik,
Automation über Hopper und Redstone sowie einem Multiblock-Reaktorsystem.

| | |
|---|---|
| **Minecraft** | 1.21.10 |
| **NeoForge** | 21.10.64 |
| **Java** | 21 |
| **Version** | 1.2.0 |
| **Lizenz** | MIT |

> Energie nutzt das **NeoForge-eigene Energiesystem** (`Capabilities.Energy` / FE) — kein externer
> Energie-Dependency mehr. Der Mod war früher ein Fabric-Mod; Details zum Port: [docs/NEOFORGE-MIGRATION.md](docs/NEOFORGE-MIGRATION.md).

---

## Features

### Items

| Item | ID | Beschreibung |
|---|---|---|
| Roh-Uran | `akw:raw_uranium` | Dropt beim Abbau von Uranerz (Fortune wirkt) |
| Uran-Barren | `akw:uranium_ingot` | Aus Roh-Uran geschmolzen |
| Angereichertes Uran | `akw:enriched_uranium` | 2× Uran-Barren craften |
| Brennstab | `akw:fuel_rod` | Reaktor-Treibstoff |
| Verbrauchter Brennstab | `akw:spent_fuel_rod` | Abfall nach Verbrennung; in Abfallbehälter lagern |
| Reaktor-Schraubenschlüssel | `akw:reactor_wrench` | Multiblock-Reaktor assemblieren/deaktivieren |

### Erz-Blöcke

| Block | ID | Eigenschaften |
|---|---|---|
| Uranerz | `akw:uranium_ore` | Braucht Eisen-Spitzhacke; spawnt y: −64 bis 32 |
| Tiefenschiefer-Uranerz | `akw:deepslate_uranium_ore` | Tiefenschiefer-Variante |

### Reaktoren (FE-Generatoren)

Brennstab in den GUI-Slot legen → Reaktor verbrennt ihn und gibt FE über alle
sechs Seiten ab. Rechtsklick öffnet das GUI mit Energie- und Hitzebalken.

| Reaktor | ID | FE/Tick | Kapazität | Brenndauer | maxHitze | Hitze/Tick |
|---|---|--:|--:|--:|--:|--:|
| Reaktor | `akw:nuclear_reactor` | 40 | 100 000 | 1600 t | 1200 | 6 |
| Fortgeschrittener Reaktor | `akw:advanced_nuclear_reactor` | 120 | 400 000 | 2000 t | 2000 | 14 |
| Brutreaktor | `akw:breeder_reactor` | 240 | 800 000 | 2200 t | 2400 | 18 |
| Thorium-Reaktor | `akw:thorium_reactor` | 180 | 600 000 | 2600 t | 2200 | 16 |
| Elite-Reaktor | `akw:elite_nuclear_reactor` | 360 | 1 600 000 | 2400 t | 3200 | 28 |
| Fusionsreaktor | `akw:fusion_reactor` | 1 000 | 4 000 000 | 1200 t | 4000 | 44 |

**Hitze-Mechanik:** Kühlrohre direkt neben dem Reaktor platzieren (−8 Hitze/Tick je
Rohr + 2 Eigenkühlung). Drosselung bei 75 % maxHitze; Explosion bei 100 %.

### Multiblock-Reaktor

Reaktor-Gehäuse-Blöcke in 3×3×3, 5×5×5 oder 7×7×7 um einen Multiblock-Controller
platzieren und mindestens einen Reaktorkern in den Innenraum setzen. Steuerstäbe neben
Kernen senken deren Wärme; Kühlrohre müssen Kerne mit der Außenhülle verbinden. Danach
mit dem Reaktor-Schraubenschlüssel assemblieren. Leistung, Wärme, Speicher und
Brennstoffverbrauch skalieren mit den tatsächlich eingebauten Kernen statt mit leerem
Innenvolumen. Der Controller besitzt Brennstoff- und Abfallslot, unterstützt Hopper
(oben Brennstoff, unten Abfall) und gibt FE an angrenzende Blöcke ab.

### Reaktor-Bausteine

| Block | ID | Verwendung |
|---|---|---|
| Reaktorkern | `akw:reactor_core` | Leistungsmodul im Multiblock und Crafting-Bauteil |
| Steuerstab-Block | `akw:control_rod_block` | Senkt Wärme angrenzender Multiblock-Kerne |
| Kühlrohr | `akw:cooling_pipe` | Kühlt Kerne über eine Verbindung zur Multiblock-Hülle |
| Blei-Block | `akw:lead_block` | Strahlenschutz (Blei auf dem Pfad Reaktor→Spieler blockt Strahlung) |
| Abfallbehälter | `akw:waste_container` | 9-Slot-GUI für Verbrauchte Brennstäbe; Komparator-Ausgang 0–15; Hopper-Support; passive Strahlung bei > 50 % Füllstand |
| Angereicherter-Uran-Block | `akw:enriched_uranium_block` | Kompaktlager (leuchtet schwach) |
| Reaktor-Gehäuse | `akw:reactor_casing` | Multiblock-Wand |
| Multiblock-Reaktor | `akw:multiblock_reactor_controller` | Controller für Multiblock-Reaktor |
| Bauroboter | `akw:reactor_builder_controller` | Baut einen 3×3×3-Multiblock automatisch aus Inventar + Energie (500 FE/Block); Roboter setzt die Blöcke |

### Energie-Infrastruktur

| Block | ID | Beschreibung |
|---|---|---|
| Energie-Kabel | `akw:energy_cable` | FE-Transport (Puffer 8 192 FE, Transfer 2 048 FE/Tick) |
| Akku-Block | `akw:energy_battery` | 1 Mio FE Speicher; Komparator-Ausgang 0–15 |

### Automation & Redstone

- **Hopper-Support:** Hopper von oben/seitig → Brennstoff einlegen; Hopper von unten → Abfall (Verbrauchter Brennstab) entnehmen. Gilt auch für den Abfallbehälter.
- **Komparator-Output (4 Modi):** Reaktoren geben wahlweise **Energie**, **Temperatur**, **Brennstoff** oder **Abfall** als Signal 0–15 aus (im GUI umschaltbar). Akku und Abfallbehälter geben ihren Füllstand 0–15 aus.
- **Redstone-Modi (4):** Im Reaktor-GUI wählbar — *Ignoriert*, *Signal aktiviert*, *Signal deaktiviert* (Standard) und *Not-Aus (SCRAM)*. SCRAM stoppt den laufenden Brennstab sofort; sonst brennt er noch ab und der Reaktor geht in Standby.

**Beispiel-Automation:** Komparator (Modus „Temperatur") am Reaktor A → Signal bei Überhitzung → Redstone → Reaktor B im Modus „Signal deaktiviert" → Reaktor B geht in Standby.

### Radioaktivität

Laufende Reaktoren bestrahlen Spieler im 8-Block-Radius (Stufe I/II je nach Hitze).
Ein **Blei-Block** auf dem direkten Pfad zwischen Reaktor und Spieler schützt vollständig.
Der Abfallbehälter strahlt bei Füllstand > 50 % (Stufe 0, 5-Block-Radius).

Visuelle Hinweise: `ELECTRIC_SPARK`-Partikel über aktiven Reaktoren; `GLOW_SQUID_INK`-Partikel bei vollem Abfallbehälter.

### Advancements

9-stufige Kette vom ersten Uranabbau bis zum Multiblock-Meister:

```
Uranabbau beginnt → Erstes Metall → Anreicherung → Brennstab bereit
  → Erster Reaktor → Energie online
       → Elite-Klasse (Challenge)
       → Kernfusion (Challenge)
       → Multiblock-Meister (Challenge)
```

---

## Installation

1. Minecraft 1.21.10 + [NeoForge 21.10.64](https://neoforged.net) installieren
2. `akw-1.2.0.jar` in den `mods`-Ordner legen (kein weiterer Dependency nötig)
3. Minecraft mit dem NeoForge-Profil starten

---

## Build (Entwickler)

Voraussetzung: **JDK 21 oder neuer**; kompiliert wird weiterhin für Java 21.

```bash
# Mod bauen (JAR unter build/libs/neoforge/)
./gradlew build

# Assets/Daten regenerieren (Client = Modelle/Sprache, Server = Rezepte/Loot/Tags/Advancements)
./gradlew runClientData
./gradlew runServerData

# Minecraft-Client zum Testen starten
./gradlew runClient
```

> Hinweis: `runClientData` und `runServerData` schreiben beide nach `src/main/generated` und löschen
> jeweils die Dateien des anderen Laufs. Für einen vollständigen Asset-Satz die `data/`-Ebene zwischen
> den Läufen sichern — siehe [docs/NEOFORGE-MIGRATION.md](docs/NEOFORGE-MIGRATION.md) §6.

**Windows und OneDrive:** Einmal `tools/prepare-onedrive.ps1` ausführen und danach
`gradlew-onedrive.bat` statt `gradlew.bat` verwenden:

```powershell
.\tools\prepare-onedrive.ps1
.\gradlew-onedrive.bat build --console=plain
```

Quellen bleiben dabei in OneDrive. Gradle-Cache und Buildausgabe liegen unter
`%LOCALAPPDATA%\AKWMod`, damit Files On-Demand keine Hash- und Gradle-Caches auslagert.

---

## Projektstruktur

```
src/main/java/ch/danielt/akw/
├─ AkwMod.java              — @Mod-Klasse (Registry + Energie-Capabilities)
├─ AkwClient.java           — @EventBusSubscriber (Screen-Registrierung, Client)
├─ block/                   — Block-Klassen (inkl. Multiblock-/Bauroboter-Controller)
├─ block/entity/            — BlockEntity-Klassen + MutableEnergyStorage (Energie-Facade)
├─ energy/                  — EnergyNet (FE-Verteilung über Capabilities.Energy)
├─ reactor/                 — weltunabhängige Multiblock-Logik (Validator, Layout, Simulation)
├─ datagen/                 — Datagen-Provider (Rezept, Loot, Modell, Tag, Lang, Advancement)
├─ screen/                  — Menüs/Screens (GUI)
├─ registry/                — ModItems, ModBlocks, ModBlockEntities, ModSounds, …
└─ worldgen/                — Uranerz-Weltgenerierung

src/main/generated/         — Datagen-Ausgabe (committed; nicht von Hand bearbeiten)
src/main/resources/         — META-INF/neoforge.mods.toml, Texturen, sounds.json, Weltgen-JSON
build/libs/neoforge/        — gebautes Mod-Jar
```

---

## Bekannte Lücken

- **Sounds:** Infrastruktur vorhanden, `.ogg`-Dateien fehlen noch → Sounds stumm.
- **Texturen:** Verbrauchter Brennstab und einige Blöcke sind Platzhalter.
- **Akku-GUI:** kein GUI; Füllstand per Komparator-Ausgang ablesbar.

Vollständiger Plan: [ROADMAP.md](ROADMAP.md) · [CHANGELOG.md](CHANGELOG.md)

---

## Dokumentation

| Datei | Inhalt |
|---|---|
| [docs/GUIDE.md](docs/GUIDE.md) | Spieler-Anleitung (Progression, Bedienung, Rezepte) |
| [docs/REFERENCE.md](docs/REFERENCE.md) | **Vollständige technische Referenz** — jede Klasse, Konstante, Formel, Blockstate, NBT-Key, Registry-ID |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Konzeptioneller Aufbau (Pakete, Datagen, Energiefluss, GUI-Kette) |
| [docs/NEOFORGE-MIGRATION.md](docs/NEOFORGE-MIGRATION.md) | Fabric→NeoForge-API-Referenz |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Beitrags-/Dev-Guide |

---

## Lizenz

MIT — siehe [LICENSE](LICENSE).  
Bugs & Feedback: [GitHub Issues](https://github.com/Brennerofhell/akw-mod/issues)
