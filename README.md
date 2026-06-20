# Atomkraftwerk (AKW) — Minecraft Fabric Mod

Fabric-Mod für Minecraft 1.21.10. Ermöglicht den Bau von Kernreaktoren, erzeugt FE-Energie
über die Team Reborn Energy API, und verwaltet Brennstäbe sowie Energie-Infrastruktur.
Der Gameplay-Loop: Uranabbau → Brennstab-Herstellung → Reaktor → Energie → Abfallmanagement.

![Minecraft 1.21.10](https://img.shields.io/badge/Minecraft-1.21.10-brightgreen)
![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)
![Java 21](https://img.shields.io/badge/Java-21-orange)

---

## Features (Stand v1.0.0)

- **6 Reaktortypen** mit steigender Leistung (Normal, Fortgeschritten, Elite, Brüter, Thorium, Fusion)
- **Energie-Kabel + Akku-Block** (Team Reborn Energy / FE, Komparator-Output 0–15)
- **Hitze & Kühlungssystem:** Kühlrohre senken Hitze, Drosselung bei 75 %, Explosion bei 100 %
- **Reaktor-GUI:** Brennstoff-Slot, Energiebalken, Hitzebalken mit Tooltips
- **Weltgenerierung** (Uranerz unterirdisch y: -64 bis 32)
- **Reaktor-Bausteine:** Reaktorkern, Kühlrohr, Steuerstab-Block, Blei-Block, Abfallbehälter

---

## Items & Blöcke

### Items
| Item | ID |
|------|----|
| Raw Uranium | `akw:raw_uranium` |
| Uranium Ingot | `akw:uranium_ingot` |
| Fuel Rod | `akw:fuel_rod` |

### Blöcke
| Block | ID |
|-------|----|
| Nuclear Reactor | `akw:nuclear_reactor` |
| Advanced Nuclear Reactor | `akw:advanced_nuclear_reactor` |
| Elite Nuclear Reactor | `akw:elite_nuclear_reactor` |
| Breeder Reactor | `akw:breeder_reactor` |
| Thorium Reactor | `akw:thorium_reactor` |
| Fusion Reactor | `akw:fusion_reactor` |
| Energy Cable | `akw:energy_cable` |
| Energy Battery | `akw:energy_battery` |
| Reactor Core | `akw:reactor_core` |
| Cooling Pipe | `akw:cooling_pipe` |
| Control Rod Block | `akw:control_rod_block` |
| Lead Block | `akw:lead_block` |
| Waste Container | `akw:waste_container` |
| Uranium Ore | `akw:uranium_ore` |
| Deepslate Uranium Ore | `akw:deepslate_uranium_ore` |
| Enriched Uranium Block | `akw:enriched_uranium_block` |

---

## Reaktor-Tier-Tabelle

| Typ | FE/t | Max FE |
|-----|-----:|-------:|
| Nuclear Reactor | 40 | 100 000 |
| Advanced Nuclear Reactor | 120 | 400 000 |
| Elite Nuclear Reactor | 360 | 1 600 000 |
| Breeder Reactor | 240 | 800 000 |
| Thorium Reactor | 180 | 600 000 |
| Fusion Reactor | 1 000 | 4 000 000 |

Alle Reaktoren bauen im Betrieb Hitze auf. Ohne Kühlung drosselt der Reaktor bei 75 %
der maximalen Hitze die Leistung auf 25 %, bei 100 % explodiert er. Kühlrohre direkt
neben dem Reaktor senken die Hitze um 8/Tick (plus 2/Tick Eigenkühlung).

---

## Installation

1. Minecraft 1.21.10 + Fabric Loader 0.19.3 installieren
2. Fabric API 0.138.4+1.21.10 in den `mods/`-Ordner legen
3. Team Reborn Energy 4.1.0 in den `mods/`-Ordner legen
4. `akw-1.0.0.jar` in den `mods/`-Ordner legen

---

## Build-Anleitung (für Entwickler)

Voraussetzung: **JDK 21**

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew build
# JAR liegt dann unter build/libs/
```

Datagen (Rezepte, Modelle, Tags, Sprach-Dateien) neu generieren:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./gradlew runDatagen
```

---

## Kompatibilität

| Komponente | Version |
|-----------|---------|
| Minecraft | 1.21.10 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.138.4+1.21.10 |
| Team Reborn Energy | 4.1.0 |
| Java | 21 |

---

## Lizenz

MIT — siehe [LICENSE](LICENSE).

---

## Bugs & Feedback

https://github.com/Brennerofhell/akw-mod/issues
