# Atomkraftwerk (AKW) — Minecraft Fabric Mod

Baue dein eigenes Atomkraftwerk: Uran abbauen, anreichern, Brennstäbe herstellen,
Reaktor betreiben und FE-kompatiblen Strom erzeugen. Mit Radioaktivitätsmechanik,
Automation über Hopper und Redstone sowie einem Multiblock-Reaktorsystem.

| | |
|---|---|
| **Minecraft** | 1.21.10 |
| **Fabric Loader** | ≥ 0.19.3 |
| **Fabric API** | 0.138.4+1.21.10 |
| **Team Reborn Energy** | 4.1.0 |
| **Java** | 21 |
| **Version** | 0.9.5 |
| **Lizenz** | MIT |

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

### Energie-Infrastruktur

| Block | ID | Beschreibung |
|---|---|---|
| Energie-Kabel | `akw:energy_cable` | FE-Transport (Puffer 8 192 FE, Transfer 2 048 FE/Tick) |
| Akku-Block | `akw:energy_battery` | 1 Mio FE Speicher; Komparator-Ausgang 0–15 |

### Automation & Redstone

- **Hopper-Support:** Hopper von oben/seitig → Brennstoff einlegen; Hopper von unten → Abfall (Verbrauchter Brennstab) entnehmen. Gilt auch für den Abfallbehälter.
- **Komparator-Output:** Reaktoren und Abfallbehälter geben Füllstand 0–15 aus — verwendbar für automatische Lastverteilung.
- **Redstone-Pause:** Redstone-Signal am Reaktor-Block stoppt das Zünden neuer Brennstäbe. Laufender Stab brennt noch ab, dann Standby.

**Beispiel-Automation:** Komparator am Reaktor A → Signal wenn voll → Redstone → Reaktor B mit Redstone-Signal belegen → Reaktor B geht in Standby.

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

1. Minecraft 1.21.10 + Fabric Loader ≥ 0.19.3 installieren
2. In den `mods/`-Ordner legen:
   - [Fabric API 0.138.4+1.21.10](https://modrinth.com/mod/fabric-api)
   - [Team Reborn Energy 4.1.0](https://github.com/TechReborn/Energy)
   - `akw-0.9.5.jar`
3. Minecraft starten

---

## Build (Entwickler)

Voraussetzung: **JDK 21 oder neuer**; kompiliert wird weiterhin für Java 21.

```bash
# Normale Arbeitskopie: Mod bauen (JAR unter build/libs/)
./gradlew build

# Assets/Daten regenerieren
./gradlew runDatagen

# Minecraft-Client zum Testen starten
./gradlew runClient
```

**Windows und OneDrive:** Einmal `tools/prepare-onedrive.ps1` ausführen und danach
`gradlew-onedrive.bat` statt `gradlew.bat` verwenden:

```powershell
.\tools\prepare-onedrive.ps1
.\gradlew-onedrive.bat build --console=plain
```

Quellen bleiben dabei in OneDrive. Gradle-Cache und Buildausgabe liegen unter
`%LOCALAPPDATA%\AKWMod`, damit Files On-Demand keine Hash- und Loom-Caches auslagert.

---

## Projektstruktur

```
src/main/java/ch/danielt/akw/
├─ AkwMod.java              — ModInitializer (Registry + Energie-Lookup)
├─ AkwClient.java           — ClientModInitializer (Screen-Registrierung)
├─ block/                   — Block-Klassen + BlockEntityProvider
├─ block/entity/            — BlockEntity-Klassen (Reaktor, Kabel, Akku, Abfall)
├─ datagen/                 — Datagen-Provider (Rezept, Loot, Modell, Tag, Lang, Advancement)
├─ screen/                  — ScreenHandler + Screen (GUI)
├─ registry/                — ModItems, ModBlocks, ModBlockEntities, ModSounds, …
└─ worldgen/                — Uranerz-Weltgenerierung

src/main/generated/         — Datagen-Ausgabe (committed; nicht von Hand bearbeiten)
src/main/resources/         — fabric.mod.json, Texturen, sounds.json, Weltgen-JSON
```

---

## Bekannte Lücken

- **Sounds:** Infrastruktur vorhanden, `.ogg`-Dateien fehlen noch → Sounds stumm.
- **Texturen:** Verbrauchter Brennstab und einige Blöcke sind Platzhalter.
- **Akku-GUI:** kein GUI; Füllstand per Komparator-Ausgang ablesbar.

Vollständiger Plan: [ROADMAP.md](ROADMAP.md) · [CHANGELOG.md](CHANGELOG.md)

---

## Lizenz

MIT — siehe [LICENSE](LICENSE).  
Bugs & Feedback: [GitHub Issues](https://github.com/Brennerofhell/akw-mod/issues)
