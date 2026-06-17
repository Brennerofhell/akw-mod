# AKW Mod - Roadmap

Entwicklungsplan für das Atomkraftwerk-Mod (Minecraft 1.21.1)

## Status: Early Development (v0.1.0)

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

### Current: v0.1.0

---

## 🎯 Phase 2: Energy System (Geplant)

### Energie-Infrastruktur
- [ ] Nuclear Reactor Block mit GUI
  - [ ] Fuel Rod-System (Verbrauch & Lagerung)
  - [ ] Energie-Generierung (FE/Joule mit Team Reborn Energy)
  - [ ] Kühlsystem-Logik
- [ ] Radiation Mechanic
  - [ ] Strahlungs-Blockeffekte
  - [ ] Spieler-Strahlungsexposition
  - [ ] Strahlungsschutz-Items

### Zielversion: v0.2.0

---

## 🔧 Phase 3: Block Expansion

### Neue Blöcke
- [ ] Enriched Uranium Block (Dekoration/Lagerung)
- [ ] Reactor Core
- [ ] Cooling Pipe System
- [ ] Control Rod Block
- [ ] Lead Block (Strahlungsschutz)
- [ ] Waste Container (Abfallmanagement)

### Zielversion: v0.3.0

---

## ⚙️ Phase 4: Advanced Mechanics

### Crafting & Processing
- [ ] Uranium anreichern (mit Rezept-System)
- [ ] Fuel Rod Crafting
- [ ] Reactor Assembly
- [ ] Abfallverarbeitung

### Automation
- [ ] Pipe-Netzwerk für Ressourcentransport
- [ ] Redstone-Integration
- [ ] Hopper-Kompatibilität

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
- [ ] Multiblock Strukturen
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
- [ ] CI/CD Pipeline (GitHub Actions)
- [ ] Automatische Builds für CurseForge/Modrinth
- [ ] Release-Automation

---

## 💡 Notizen

- **Team Reborn Energy** ist bereits integriert → Energiesystem kann direkt in Phase 2 starten
- **Resourcen-Resourcen** (Texturen/Sounds) sind aktuell nicht vorhanden → müssen für v1.0.0 erstellt werden
- **Modversion-Kompatibilität**: Dieses Mod fokussiert auf Minecraft 1.21.1 - Rückwärts-Kompatibilität wird später evaluiert

---

## 📞 Kontakt & Feedback

Für Vorschläge und Bug-Reports: Issues in diesem Repository
