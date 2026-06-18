# Atomkraftwerk (AKW) — Minecraft Fabric Mod

Baue dein eigenes Atomkraftwerk: Uran abbauen, Brennstäbe herstellen, Reaktor
betreiben und FE-kompatiblen Strom erzeugen.

| | |
|---|---|
| **Minecraft** | 1.21.10 |
| **Mod Loader** | Fabric Loader ≥ 0.16.0 |
| **Java** | 21 |
| **Version** | 0.3.0 (Energie-Infrastruktur + Kühlsystem) |
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

**Hitze-Mechanik:** Reaktoren bauen im Betrieb Hitze auf. Ohne Kühlung drosselt der
Reaktor bei 75 % der maximalen Hitze die Leistung auf 25 %, bei 100 % explodiert er.
Platziere **Kühlrohre** direkt neben dem Reaktor (je 1 Rohr = −8 Hitze/Tick, dazu 2
Eigenkühlung). Das GUI zeigt einen Hitzebalken (orange → rot ab Drosselung).

| Reaktor | ID | Kapazität | FE/Tick | Brenndauer | maxHitze | Hitze/Tick |
|---|---|--:|--:|--:|--:|--:|
| Reaktor | `akw:nuclear_reactor` | 100 000 | 40 | 1600 t | 1200 | 6 |
| Fortgeschrittener Reaktor | `akw:advanced_nuclear_reactor` | 400 000 | 120 | 2000 t | 2000 | 14 |
| Brutreaktor | `akw:breeder_reactor` | 800 000 | 240 | 2200 t | 2400 | 18 |
| Thorium-Reaktor | `akw:thorium_reactor` | 600 000 | 180 | 2600 t | 2200 | 16 |
| Elite-Reaktor | `akw:elite_nuclear_reactor` | 1 600 000 | 360 | 2400 t | 3200 | 28 |
| Fusionsreaktor | `akw:fusion_reactor` | 4 000 000 | 1 000 | 1200 t | 4000 | 44 |

### Reaktor-Bausteine
| Block | ID | Verwendung |
|---|---|---|
| Reaktorkern | `akw:reactor_core` | Crafting-Bauteil (leuchtet schwach) |
| Steuerstab-Block | `akw:control_rod_block` | Bauteil fortgeschr. Reaktoren |
| Kühlrohr | `akw:cooling_pipe` | Kühlung (−8 Hitze/Tick je angrenzendem Rohr) |
| Blei-Block | `akw:lead_block` | Strahlenschutz (Deko/Lager) |
| Abfallbehälter | `akw:waste_container` | Lager (Deko) |
| Angereicherter-Uran-Block | `akw:enriched_uranium_block` | Kompaktlager (leuchtet schwach) |

### Energie-Infrastruktur
| Block | ID | Beschreibung |
|---|---|---|
| Energie-Kabel | `akw:energy_cable` | FE-Transport zwischen Blöcken (Puffer 8 192 FE, Transfer 2 048 FE/Tick) |
| Akku-Block | `akw:energy_battery` | Großer FE-Speicher (1 Mio FE); Komparator-Ausgang zeigt Füllstand 0–15 |

Beide Blöcke exponieren `EnergyStorage.SIDED` → automatisch **FE-kompatibel mit
Create** (via FE-Brücken) und anderen Tech-Mods.

### Mechaniken
- **Energie:** Reaktoren sind FE-Generatoren (Team Reborn Energy-API); Kabel & Akku
  transportieren/speichern FE; kompatibel mit allen FE-Maschinen.
- **Hitze & Kühlung:** Reaktor baut Hitze auf → Kühlrohre senken sie; Drosselung bei
  75 %, Explosion bei 100 % maxHitze (tier-abhängige Stärke).
- **GUI:** Brennstoff-Slot, Energiebalken, Hitzebalken (orange/rot) mit Tooltips;
  serverseitige Synchronisation per `PropertyDelegate` (6 Indizes).
- **Loot-Tables:** Erze droppen Roh-Uran (Silk Touch → Block, Fortune erhöht Drop);
  alle übrigen Blöcke droppen sich selbst.
- **Mining-Tags:** Alle AKW-Blöcke sind `mineable/pickaxe`; Erze zusätzlich `needs_iron_tool`.

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
└─ src/main/
   ├─ generated/               # Datagen-Ausgabe (committed; NICHT von Hand bearbeiten)
   ├─ java/ch/danielt/akw/
   │  ├─ AkwMod.java           # ModInitializer (Registry + Energie-Lookup)
   │  ├─ AkwClient.java        # ClientModInitializer (Screen-Registrierung)
   │  ├─ energy/
   │  │  └─ EnergyNet.java     # Gemeinsame FE-Push-Logik (Reaktor, Kabel, Akku)
   │  ├─ block/
   │  │  ├─ NuclearReactorBlock.java         # FACING/LIT, GUI, Ticker (pro Tier parametr.)
   │  │  ├─ EnergyCableBlock.java            # FE-Kabel (Puffer + Ticker)
   │  │  ├─ EnergyBatteryBlock.java          # FE-Akku (Komparator-Ausgang)
   │  │  └─ entity/
   │  │     ├─ NuclearReactorBlockEntity.java # Energie, Hitze, Brennstab-Logik
   │  │     ├─ EnergyCableBlockEntity.java    # FE-Puffer + Push
   │  │     ├─ EnergyBatteryBlockEntity.java  # FE-Speicher + Komparator
   │  │     └─ ImplementedInventory.java      # Inventory-Helfer
   │  ├─ datagen/
   │  │  ├─ AkwDataGenerator.java     # Datagen-Einstiegspunkt
   │  │  ├─ ModModelProvider.java     # Blockstates + Modelle
   │  │  ├─ ModRecipeProvider.java    # Rezepte
   │  │  ├─ ModLootTableProvider.java # Loot-Tables
   │  │  ├─ ModTagsProvider.java      # Block-Tags
   │  │  └─ ModLanguageProvider.java  # de_de + en_us
   │  ├─ screen/
   │  │  ├─ NuclearReactorScreenHandler.java  # Slots, PropertyDelegate (6 Indizes)
   │  │  └─ NuclearReactorScreen.java         # GUI (Energie- + Hitzebalken)
   │  └─ registry/
   │     ├─ ModItems.java         # Items
   │     ├─ ModBlocks.java        # Reaktoren (REACTORS), Bausteine (DECOR), Infrastruktur
   │     ├─ ModBlockEntities.java # BE-Typen (Reaktor, Kabel, Akku)
   │     ├─ ModScreenHandlers.java# ExtendedScreenHandlerType (BlockPos-Sync)
   │     └─ ModItemGroups.java    # Kreativ-Tab + Vanilla-Tab-Einträge
   └─ resources/
      ├─ fabric.mod.json       # Mod-Metadaten (inkl. icon)
      ├─ assets/akw/
      │  ├─ icon.png           # Mod-Logo (512×512)
      │  └─ textures/          # Manuelle Texturen (block/, item/, gui/)
      └─ data/akw/
         └─ worldgen/          # Uranerz-Weltgenerierung
```

> **Daten regenerieren:** Rezepte, Loot-Tables, Modelle, Tags und Lang-Dateien werden per
> Fabric Datagen (Java) erzeugt. Ausgabe geht nach `src/main/generated/` (committed).
> Nach Änderungen an Datagen-Klassen: `./gradlew runDatagen`.

---

## 🔨 Build

Voraussetzung: **JDK 21**. Der Pfad ist in `gradle.properties` fest hinterlegt
(`org.gradle.java.home`) — ggf. an dein System anpassen.

```bash
# Mod kompilieren und JAR bauen (Ausgabe: build/libs/akw-0.3.0.jar)
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

- **Texturen** für `energy_cable` und `energy_battery` sind vorläufige Platzhalter
  (einfarbige Blöcke) — echte Pixel-Art folgt in v1.0 Politur.
- **Akku-GUI** fehlt noch; Füllstand ist über den Komparator-Ausgang (0–15) ablesbar.
- **Hitzebalken-Position** (`x+137`) ist ein Schätzwert — In-Game-Verifizierung steht aus.
- **Steuerstab-Wirkung und Redstone-SCRAM** folgen in v0.4 (Reaktorsteuerung).
- **Dampf & Turbine** (v0.5), **Strahlung** (v0.7), **Meltdown** (v0.8) sind geplant
  (siehe [ROADMAP.md](ROADMAP.md)).

Vollständiger Plan: siehe [ROADMAP.md](ROADMAP.md).

---

## 📦 Abhängigkeiten

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Team Reborn Energy](https://maven.fabricmc.net/) — wird per *Jar-in-Jar* (`include`) mitgebündelt

---

## 📜 Lizenz

MIT — siehe [LICENSE](LICENSE).
