# AKW Mod - Roadmap

Entwicklungsplan für das Atomkraftwerk-Mod (Minecraft 1.21.10 / NeoForge 21.10.64)

> Die Migration von Fabric auf NeoForge ist abgeschlossen (v1.1.0) — siehe
> [docs/NEOFORGE-MIGRATION.md](docs/NEOFORGE-MIGRATION.md). Die folgenden Phasen sind plattformneutral.

## Status: v1.2.0 — Bauroboter + konfigurierbare Redstone-/Komparator-Modi

---

## ✅ Phase 1: Foundation (Aktuell)

### Core Systems
- [x] Mod-Initialisierung und Registry-System
- [x] Basis-Items: Raw Uranium, Uranium Ingot, Fuel Rod
- [x] Ore Blöcke: Uranium Ore, Deepslate Uranium Ore
- [x] Item Group / Creative Tab
- [x] Mehrsprachigkeit (Deutsch, Englisch)
- [x] Loot-Tables für Erze (Roh-Uran-Drop, Silk-Touch/Fortune)
- [x] Mining-Tags (`mineable/pickaxe`, `needs_iron_tool`)
- [x] Rezepte (Schmelzen/Schmelzofen → Barren, Crafting → Brennstab)
- [x] Projekt-Doku (LICENSE, README, CHANGELOG)
- [x] Texturen (Items, Blöcke, Mod-Icon) — alle 6 Item- und 38 Block-Texturen sowie
      `assets/akw/icon.png` (512×512) vorhanden
- [x] Erz-Weltgenerierung (Worldgen) — Uranerz spawnt untertage (y -64…32)

---

## 🎯 Phase 2: Energy System (Großteils umgesetzt)

### Energie-Infrastruktur
- [x] Nuclear Reactor Block mit GUI (**6 Typen**: Reaktor, Fortgeschritten,
      Elite, Brutreaktor, Thorium, Fusion)
  - [x] Fuel Rod-System (Verbrauch im Brennstoff-Slot)
  - [x] Energie-Generierung (FE über die NeoForge-Energie-Capability, Abgabe an alle Seiten)
  - [x] Kühlsystem-Logik (Hitzeaufbau, Kühlrohr-Kühlung, Drosselung, Überhitzung→Explosion, Hitzebalken im GUI)
- [x] Energie-Kabel (FE-Transport zwischen Blöcken, Puffer 8 192 FE)
- [x] Akku-Block (1 Mio FE Speicher, Komparator-Signal 0–15)
- [x] Radiation Mechanic (v0.5.0)
  - [x] Strahlungs-Blockeffekte (pfadbasierte Blei-Block-Abschirmung, v0.7.0)
  - [x] Spieler-Strahlungsexposition (8-Block-Radius, Stufen I/II je nach Hitze)
  - [x] Strahlungsschutz-Items (Blei-Block bereits vorhanden)

✅ Reaktoren & Energie ausgeliefert (v0.2.0).

---

## 🔧 Phase 3: Block Expansion

### Neue Blöcke (Bausteine als Blöcke vorhanden — Funktionslogik folgt)
- [x] Enriched Uranium Block (Dekoration/Lagerung)
- [x] Reactor Core
- [x] Cooling Pipe (kühlt angrenzende Reaktoren; Rohr-Netzwerk-Logik weiterhin offen)
- [x] Control Rod Block
- [x] Lead Block (Strahlungsschutz-Block vorhanden)
- [x] Waste Container (Block vorhanden; Abfall-Logik offen)

### Zielversion: v0.3.7 ✅ (ausgeliefert)

---

## ⚙️ Phase 4: Advanced Mechanics

### Crafting & Processing
- [x] Uranium anreichern (2× Uran-Barren → Angereichertes Uran → Brennstab)
- [x] Fuel Rod Crafting
- [x] Abfallverarbeitung: Verbrauchter Brennstab (v0.7.0)
- [x] Reactor Assembly (Multiblock-Controller + Schraubenschlüssel, v0.6.0;
      automatischer Bauroboter v1.2.0)
- [ ] Nuklear-Abfall weiterverarbeiten (Recycling / Endlager)

### Automation
- [x] Hopper-Kompatibilität (v0.7.0): oben → Brennstoff, unten → Abfall
- [x] Redstone-Integration (v0.8.0): Komparator-Output + POWERED-Pause
- [x] Konfigurierbare Redstone-Modi (4) + Komparator-Modi (4) im GUI (v1.2.0):
      Ignoriert / Signal aktiviert / Signal deaktiviert / Not-Aus (SCRAM) bzw.
      Energie / Temperatur / Brennstoff / Abfall
- [ ] Pipe-Netzwerk für Ressourcentransport

### Zielversion: v0.4.0

---

## 📊 Phase 5: Quality & Polish

### Content
- [x] Vollständige Texturen (alle Blöcke & Items)
- [x] Sounds & Effekte (v0.9.5 — Infrastruktur + Partikel vorhanden)
  - [ ] **`.ogg`-Sounddateien fehlen** — `sounds.json` definiert 3 Events
        (`reactor_ambient`, `reactor_alert`, `reactor_meltdown`) mit leeren `sounds`-Arrays;
        kein `assets/akw/sounds/`-Verzeichnis vorhanden
- [x] Partikel für Radioaktivität (v0.9.5 — vanilla ELECTRIC_SPARK)
- [x] Advancement/Achievement System (v0.9.5 — 9-stufige Kette)

### Dokumentation
- [x] Technische Referenz (`docs/REFERENCE.md`, v1.2.0 — klassengenau) + Spieler-Guide
      (`docs/GUIDE.md`), Architektur (`docs/ARCHITECTURE.md`)
- [ ] In-Game-Wiki / Manual
- [ ] Config-System für Balance-Einstellungen

### Zielversion: v1.0.0

---

## 🚀 Phase 6: Extensions (Post-Release)

- [ ] Weitere Brennstoffe/Materialien
- [x] Multiblock Strukturen (v0.6.0: 3×3×3, 5×5×5, 7×7×7)
- [x] Bauroboter — automatischer 3×3×3-Multiblock-Aufbau aus Inventar + Energie (v1.2.0)
- [ ] Netzwerk-System zwischen Reaktoren
- [ ] Mod-Kompatibilität (andere Tech-Mods)
- [ ] Mehrsprachige Erweiterung

---

## 📋 Technisches Backlog

### Code Quality
- [ ] Unit Tests für Energy-Berechnung
- [ ] Block-Interaktions-Tests
- [ ] Kompatibilität-Tests mit anderen Mods
- [ ] Performance-Optimierung

### Infrastructure
- [x] CI/CD Pipeline (GitHub Actions, v0.4.1)
- [ ] Automatische Builds für CurseForge/Modrinth
- [ ] Release-Automation

---

## 💡 Notizen

- **Energiesystem** nutzt die NeoForge-eigene Energie-Capability (FE) → kein externer Energie-Dependency
- **Ressourcen**: Texturen (Items/Blöcke) und Mod-Icon sind vollständig vorhanden;
  einzig die `.ogg`-Sounddateien fehlen noch (Infrastruktur in `sounds.json` ist da)
- **Modversion-Kompatibilität**: Dieses Mod fokussiert auf Minecraft 1.21.10 - Rückwärts-Kompatibilität wird später evaluiert

---

## 📞 Kontakt & Feedback

Für Vorschläge und Bug-Reports: Issues in diesem Repository
