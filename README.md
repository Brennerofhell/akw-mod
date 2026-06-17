# Atomkraftwerk (AKW) — Minecraft Fabric Mod

Baue dein eigenes Atomkraftwerk: Uran abbauen, Brennstäbe herstellen, Reaktor
betreiben und FE-kompatiblen Strom erzeugen.

| | |
|---|---|
| **Minecraft** | 1.21.1 |
| **Mod Loader** | Fabric Loader ≥ 0.16.0 |
| **Java** | 21 |
| **Version** | 0.2.0 (Energiesystem) |
| **Lizenz** | MIT |

---

## 📚 Dokumentation

- **[docs/GUIDE.md](docs/GUIDE.md)** — Spieler-Guide: Progression, Reaktor-Bedienung,
  alle Reaktor-Werte und Rezepte.
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — Entwickler-Guide: Paketstruktur,
  datengetriebene Asset-Pipeline, Energiefluss, GUI-Kette, „neuen Reaktor-Typ hinzufügen".
- **[CHANGELOG.md](CHANGELOG.md)** · **[ROADMAP.md](ROADMAP.md)**

---

## ✨ Features (aktueller Stand)

### Items
| Item | ID | Beschreibung |
|---|---|---|
| Roh-Uran | `akw:raw_uranium` | Dropt beim Abbau von Uranerz |
| Uran-Barren | `akw:uranium_ingot` | Aus Roh-Uran geschmolzen |
| Brennstab | `akw:fuel_rod` | Aus Uran-Barren gecraftet (späterer Reaktor-Treibstoff) |

### Erz-Blöcke
| Block | ID | Eigenschaften |
|---|---|---|
| Uranerz | `akw:uranium_ore` | Wie Eisenerz; braucht **Eisen-Spitzhacke**; dropt Roh-Uran (Glück wirkt) |
| Tiefenschiefer-Uranerz | `akw:deepslate_uranium_ore` | Tiefenschiefer-Variante |

### Reaktoren (funktional, FE-Generatoren)

Alle Reaktoren teilen dieselbe Mechanik: Brennstab in den GUI-Slot legen → der
Reaktor verbrennt ihn über `burn`-Ticks, erzeugt dabei `FE/Tick` und gibt den Strom
über **alle sechs Seiten** an angrenzende FE-Speicher/Maschinen ab (Team Reborn
Energy). Bei Betrieb leuchtet die Front (`lit`-Blockstate). Rechtsklick öffnet das GUI.

| Reaktor | ID | Kapazität (FE) | FE/Tick | Abgabe/Tick | Brenndauer/Stab |
|---|---|--:|--:|--:|--:|
| Reaktor | `akw:nuclear_reactor` | 100 000 | 40 | 512 | 1600 |
| Fortgeschrittener Reaktor | `akw:advanced_nuclear_reactor` | 400 000 | 120 | 2 048 | 2000 |
| Elite-Reaktor | `akw:elite_nuclear_reactor` | 1 600 000 | 360 | 8 192 | 2400 |
| Brutreaktor | `akw:breeder_reactor` | 800 000 | 240 | 4 096 | 2200 |
| Thorium-Reaktor | `akw:thorium_reactor` | 600 000 | 180 | 3 072 | 2600 |
| Fusionsreaktor | `akw:fusion_reactor` | 4 000 000 | 1 000 | 32 768 | 1200 |

### Reaktor-Bausteine
| Block | ID | Verwendung |
|---|---|---|
| Reaktorkern | `akw:reactor_core` | Crafting-Bauteil (leuchtet schwach) |
| Steuerstab-Block | `akw:control_rod_block` | Bauteil fortgeschr. Reaktoren |
| Kühlrohr | `akw:cooling_pipe` | Bauteil des Elite-Reaktors |
| Blei-Block | `akw:lead_block` | Strahlenschutz (Deko/Lager) |
| Abfallbehälter | `akw:waste_container` | Lager (Deko) |
| Angereicherter-Uran-Block | `akw:enriched_uranium_block` | Kompaktlager (leuchtet schwach) |

### Mechaniken
- **Energie:** Reaktoren sind FE-Generatoren über die **Team Reborn Energy**-API
  (kompatibel mit Tech-Reborn-/FE-Maschinen & -Kabeln).
- **GUI:** Brennstoff-Slot, Energiebalken (Tooltip zeigt `FE / Kapazität`) und
  Brenn-Anzeige; Werte werden serverseitig per `PropertyDelegate` synchronisiert.
- **Loot-Tables:** Erze droppen Roh-Uran (Silk Touch → Block, Fortune erhöht Drop);
  alle übrigen Blöcke droppen sich selbst.
- **Mining-Tags:** Alle AKW-Blöcke sind `mineable/pickaxe`; Erze zusätzlich `needs_iron_tool`.
- **Rezepte:** Schmelzen (Roh-Uran → Barren), Crafting (Barren → Brennstab),
  sowie Crafting-Rezepte für alle Reaktoren und Bausteine (gestaffelt: höhere
  Tiers verbauen den jeweils kleineren Reaktor).

---

## 📁 Projektstruktur

```
akw mod/
├─ build.gradle                # Fabric Loom Build-Konfiguration
├─ gradle.properties           # Versionen (MC, Yarn, Fabric, Energy)
├─ settings.gradle
├─ LICENSE                     # MIT
├─ README.md / CHANGELOG.md / ROADMAP.md
├─ briefkasten/                # Austausch-Ordner (nicht Teil des Builds)
├─ tools/                      # Generatoren (reine Python-stdlib, kein PIL)
│  ├─ akw_data.py              # ZENTRALE Datenquelle: Reaktor-Typen & Bausteine
│  ├─ gen_textures.py          # erzeugt alle PNG-Texturen + Mod-Icon + GUI
│  └─ gen_resources.py         # erzeugt Blockstates/Modelle/Loot/Rezepte/Tags/Lang
└─ src/main/
   ├─ java/ch/danielt/akw/
   │  ├─ AkwMod.java           # ModInitializer (Registry + Energie-Lookup)
   │  ├─ AkwClient.java        # ClientModInitializer (Screen-Registrierung)
   │  ├─ block/
   │  │  ├─ NuclearReactorBlock.java         # FACING/LIT, GUI, Ticker (pro Tier parametr.)
   │  │  └─ entity/
   │  │     ├─ NuclearReactorBlockEntity.java # Energie, Brennstab-Logik, FE-Abgabe
   │  │     └─ ImplementedInventory.java      # Inventory-Helfer
   │  ├─ screen/
   │  │  ├─ NuclearReactorScreenHandler.java  # Slots, PropertyDelegate, quickMove
   │  │  └─ NuclearReactorScreen.java         # GUI-Rendering (Client)
   │  └─ registry/
   │     ├─ ModItems.java         # Items
   │     ├─ ModBlocks.java        # Erze, Reaktoren (Liste REACTORS), Bausteine (DECOR)
   │     ├─ ModBlockEntities.java # ein gemeinsamer BE-Typ für alle Reaktoren
   │     ├─ ModScreenHandlers.java# ExtendedScreenHandlerType (BlockPos-Sync)
   │     └─ ModItemGroups.java    # Kreativ-Tab
   └─ resources/
      ├─ fabric.mod.json       # Mod-Metadaten (inkl. icon)
      ├─ assets/akw/
      │  ├─ icon.png           # Mod-Logo (512×512)
      │  ├─ lang/              # de_de, en_us
      │  ├─ models/            # Item- & Block-Modelle
      │  ├─ blockstates/       # Blockstates
      │  └─ textures/          # block/, item/, gui/ (alle generiert)
      └─ data/akw/
         ├─ loot_table/blocks/ # Block-Drops
         ├─ recipe/            # Schmelz- & Craft-Rezepte
         └─ worldgen/          # Uranerz-Weltgenerierung
```

> **Daten regenerieren:** Rezepte, Loot-Tables, Modelle, Tags und Lang-Dateien werden per
> Fabric Datagen (Java) erzeugt. Nach Änderungen an den Datagen-Klassen:
> `./gradlew runDatagen` (schreibt direkt nach `src/main/resources/`).

---

## 🔨 Build

Voraussetzung: **JDK 21**. Der Pfad ist in `gradle.properties` fest hinterlegt
(`org.gradle.java.home`) — ggf. an dein System anpassen.

```bash
# Mod kompilieren und JAR bauen (Ausgabe: build/libs/akw-0.1.0.jar)
./gradlew build

# Minecraft-Client mit Mod zum Testen starten
./gradlew runClient

# Dedizierten Server starten
./gradlew runServer
```

---

## 🧱 Code-Konventionen

- **Registrierungs-Pattern:** Statische Felder in den `Mod*`-Klassen werden über
  `register(...)`-Helfer angemeldet. `registerAll()` erzwingt das Klassen-Laden
  (und damit die Feld-Initialisierung) aus `AkwMod.onInitialize()`.
- **Mod-ID:** `akw` (Konstante `AkwMod.MOD_ID`).
- **Identifier:** immer via `Identifier.of(AkwMod.MOD_ID, name)`.
- **Sprache im Code/Logs:** Deutsch.

---

## ⚠️ Bekannte Lücken / To-do

- **Texturen** sind generierte Pixel-Art-Platzhalter (`tools/gen_textures.py`) — für
  ein Release ggf. durch handgemalte ersetzen (Specs: `briefkasten/ausgang/`).
- **Bausteine sind noch passiv:** Kühlung, Strahlung, Steuerstab-Wirkung und
  Multiblock-Strukturen sind als spätere Phasen geplant (siehe `ROADMAP.md`).
- **Balancing** der Reaktor-Werte ist vorläufig; alle Reaktoren nutzen denselben
  Brennstab (`akw:fuel_rod`).
- **Validierung:** Build (`./gradlew build`) und ein headless `runServer` laden alle
  Registries/Rezepte/Loot fehlerfrei; die visuelle In-Game-Prüfung via `runClient`
  steht als manueller Schritt aus.

Vollständiger Plan: siehe [ROADMAP.md](ROADMAP.md).

---

## 📦 Abhängigkeiten

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Team Reborn Energy](https://maven.fabricmc.net/) — wird per *Jar-in-Jar* (`include`) mitgebündelt

---

## 📜 Lizenz

MIT — siehe [LICENSE](LICENSE).
