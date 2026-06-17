# Atomkraftwerk (AKW) — Minecraft Fabric Mod

Baue dein eigenes Atomkraftwerk: Uran abbauen, Brennstäbe herstellen, Reaktor
betreiben und FE-kompatiblen Strom erzeugen.

| | |
|---|---|
| **Minecraft** | 1.21.1 |
| **Mod Loader** | Fabric Loader ≥ 0.16.0 |
| **Java** | 21 |
| **Version** | 0.1.0 (early development) |
| **Lizenz** | MIT |

---

## ✨ Features (aktueller Stand)

### Items
| Item | ID | Beschreibung |
|---|---|---|
| Roh-Uran | `akw:raw_uranium` | Dropt beim Abbau von Uranerz |
| Uran-Barren | `akw:uranium_ingot` | Aus Roh-Uran geschmolzen |
| Brennstab | `akw:fuel_rod` | Aus Uran-Barren gecraftet (späterer Reaktor-Treibstoff) |

### Blöcke
| Block | ID | Eigenschaften |
|---|---|---|
| Uranerz | `akw:uranium_ore` | Wie Eisenerz; braucht **Eisen-Spitzhacke**; dropt Roh-Uran (Glück wirkt) |
| Tiefenschiefer-Uranerz | `akw:deepslate_uranium_ore` | Tiefenschiefer-Variante |

### Mechaniken
- **Loot-Tables:** Erze droppen Roh-Uran, mit **Behutsamkeit (Silk Touch)** den Block selbst, **Glück (Fortune)** erhöht den Drop.
- **Mining-Tags:** Erze sind als `mineable/pickaxe` + `needs_iron_tool` registriert.
- **Rezepte:** Schmelzen/Schmelzofen (Roh-Uran → Barren) und Crafting (3× Barren → Brennstab).
- **Energie:** Team Reborn Energy API ist eingebunden (FE-kompatibel) — Reaktor-Logik folgt (Phase 2).

---

## 📁 Projektstruktur

```
akw mod/
├─ build.gradle                # Fabric Loom Build-Konfiguration
├─ gradle.properties           # Versionen (MC, Yarn, Fabric, Energy)
├─ settings.gradle
├─ LICENSE                     # MIT
├─ README.md                   # diese Datei
├─ CHANGELOG.md                # Versions-Historie
├─ ROADMAP.md                  # Entwicklungsplan
├─ briefkasten/                # Austausch-Ordner (nicht Teil des Builds)
└─ src/main/
   ├─ java/ch/danielt/akw/
   │  ├─ AkwMod.java           # ModInitializer (Einstiegspunkt)
   │  ├─ AkwClient.java        # ClientModInitializer
   │  └─ registry/
   │     ├─ ModItems.java      # Item-Registrierung
   │     ├─ ModBlocks.java     # Block-Registrierung (+ BlockItems)
   │     └─ ModItemGroups.java # Kreativ-Tab
   └─ resources/
      ├─ fabric.mod.json       # Mod-Metadaten
      ├─ assets/akw/
      │  ├─ lang/              # de_de, en_us
      │  ├─ models/            # Item- & Block-Modelle
      │  └─ blockstates/       # Blockstates
      │  └─ textures/          # ⚠️ FEHLT NOCH (siehe unten)
      └─ data/akw/
         ├─ loot_table/blocks/ # Erz-Drops
         └─ recipe/            # Schmelz- & Craft-Rezepte
```

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

- **Texturen fehlen komplett** — alle Modelle zeigen auf nicht vorhandene PNGs
  (im Spiel als fehlende Textur sichtbar). Benötigt:
  - `assets/akw/textures/item/{raw_uranium,uranium_ingot,fuel_rod}.png` (16×16)
  - `assets/akw/textures/block/{uranium_ore,deepslate_uranium_ore}.png` (16×16)
  - `assets/akw/icon.png` (512×512 Mod-Logo) + Eintrag in `fabric.mod.json`
- **Reaktor & Energiesystem** (Phase 2) noch offen — geplant als volle Variante
  mit GUI, Brennstab-Slots und Team-Reborn-Energiespeicher (siehe `ROADMAP.md`).
- **Rezept-Formate** sind auf MC 1.21.1 ausgelegt; nach dem ersten `runClient`
  das Log auf Recipe-/Loot-Parse-Fehler prüfen.

Vollständiger Plan: siehe [ROADMAP.md](ROADMAP.md).

---

## 📦 Abhängigkeiten

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Team Reborn Energy](https://maven.fabricmc.net/) — wird per *Jar-in-Jar* (`include`) mitgebündelt

---

## 📜 Lizenz

MIT — siehe [LICENSE](LICENSE).
