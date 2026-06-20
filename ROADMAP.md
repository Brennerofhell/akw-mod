# AKW Mod - Roadmap

Entwicklungsplan für das Atomkraftwerk-Mod (Minecraft 1.21.10)

## Status: In Entwicklung (v0.7.0 — Hopper-Support, Abfall-Slot, Strahlungsabschirmung)

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
- [ ] Texturen (Items, Blöcke, Mod-Icon) — Specs in `briefkasten/ausgang/`
- [x] Erz-Weltgenerierung (Worldgen) — Uranerz spawnt untertage (y -64…32)

### Current: v0.7.0

---

## 🎯 Phase 2: Energy System (Großteils umgesetzt)

### Energie-Infrastruktur
- [x] Nuclear Reactor Block mit GUI (**6 Typen**: Reaktor, Fortgeschritten,
      Elite, Brutreaktor, Thorium, Fusion)
  - [x] Fuel Rod-System (Verbrauch im Brennstoff-Slot)
  - [x] Energie-Generierung (FE mit Team Reborn Energy, Abgabe an alle Seiten)
  - [x] Kühlsystem-Logik (Hitzeaufbau, Kühlrohr-Kühlung, Drosselung, Überhitzung→Explosion, Hitzebalken im GUI)
- [x] Energie-Kabel (FE-Transport zwischen Blöcken, Puffer 8 192 FE)
- [x] Akku-Block (1 Mio FE Speicher, Komparator-Signal 0–15)
- [x] Radiation Mechanic (v0.5.0)
  - [x] Strahlungs-Blockeffekte (pfadbasierte Blei-Block-Abschirmung, v0.7.0)
  - [x] Spieler-Strahlungsexposition (8-Block-Radius, Stufen I/II je nach Hitze)
  - [x] Strahlungsschutz-Items (Blei-Block bereits vorhanden)

### Zielversion: v0.2.0 ✅ (Reaktoren & Energie ausgeliefert)

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
- [ ] Reactor Assembly
- [ ] Nuklear-Abfall weiterverarbeiten (Recycling / Endlager)

### Automation
- [x] Hopper-Kompatibilität (v0.7.0): oben → Brennstoff, unten → Abfall
- [ ] Pipe-Netzwerk für Ressourcentransport
- [ ] Redstone-Integration

### Zielversion: v0.4.0

---

## 📊 Phase 5: Quality & Polish

### Content
- [ ] Vollständige Texturen (alle Blöcke & Items)
- [ ] Sounds & Effekte
- [ ] Partikel für Radioaktivität
- [ ] Advancement/Achievement System

### Dokumentation
- [ ] In-Game-Wiki / Manual
- [ ] Mod-Anleitung
- [ ] Config-System für Balance-Einstellungen

### Zielversion: v1.0.0

---

## 🚀 Phase 6: Extensions (Post-Release)

- [ ] Weitere Brennstoffe/Materialien
- [x] Multiblock Strukturen (v0.6.0: 3×3×3, 5×5×5, 7×7×7)
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

- **Team Reborn Energy** ist bereits integriert → Energiesystem kann direkt in Phase 2 starten
- **Resourcen-Resourcen** (Texturen/Sounds) sind aktuell nicht vorhanden → müssen für v1.0.0 erstellt werden
- **Modversion-Kompatibilität**: Dieses Mod fokussiert auf Minecraft 1.21.10 - Rückwärts-Kompatibilität wird später evaluiert

---

## 📞 Kontakt & Feedback

Für Vorschläge und Bug-Reports: Issues in diesem Repository
