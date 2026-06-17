# 🎨 Grafik-Anweisungen für die AKW-Mod

Hier steht **genau**, welche Grafiken du erstellen sollst, in welcher Größe und
wohin sie kommen. Lege die fertigen PNGs einfach in `briefkasten/eingang/` ab —
ich baue sie dann an die richtige Stelle ins Projekt ein.

> **Format:** Alle Texturen sind **PNG** mit **Transparenz** (RGBA).
> Minecraft nutzt **Pixel-Art** ohne Weichzeichner — male „hart", Pixel für Pixel,
> kein Anti-Aliasing.

---

## 1) Item-Texturen — je **16 × 16 px**

| Datei (Name genau so) | Stellt dar | Stil-Hinweise |
|---|---|---|
| `raw_uranium.png` | Roh-Uran-Brocken | Unregelmäßiger Klumpen, grau-grünlich, leuchtend-grüne Sprenkel (radioaktiv) |
| `uranium_ingot.png` | Uran-Barren | Klassische Barren-Form (wie Eisenbarren), mattgrün/grau-metallisch |
| `fuel_rod.png` | Brennstab | Schlanker vertikaler Stab, metallische Hülle, oben/unten Kappe, grüner Glüh-Akzent |

---

## 2) Block-Texturen — je **16 × 16 px**

| Datei | Stellt dar | Stil-Hinweise |
|---|---|---|
| `uranium_ore.png` | Uranerz | Stein-Hintergrund (wie Vanilla `stone`) mit grünen Erz-Einsprengseln |
| `deepslate_uranium_ore.png` | Tiefenschiefer-Uranerz | Dunkler Tiefenschiefer-Hintergrund, gleiche grüne Einsprengsel |

> Tipp: Damit es sich nahtlos einfügt, kannst du den Stein-/Tiefenschiefer-Hintergrund
> von den Vanilla-Erzen übernehmen und nur die Erz-Punkte grün einfärben.

---

## 3) Mod-Icon / Logo — **512 × 512 px**

| Datei | Zweck |
|---|---|
| `icon.png` | Mod-Logo (zeigt in Mod-Liste, CurseForge/Modrinth) |

- Quadratisch, darf detaillierter sein als die 16×16-Texturen (kein Pixel-Art-Zwang).
- Motiv-Idee: Kühlturm/Reaktor-Symbol + Radioaktiv-Zeichen, grün-gelbe Akzente.

---

## 4) (Später, für Phase 2 — Reaktor) — noch **nicht dringend**

Brauchst du erst, wenn wir den Reaktor bauen. Nur zur Info:

| Datei | Größe | Zweck |
|---|---|---|
| `nuclear_reactor_top.png` | 16×16 | Reaktor-Oberseite |
| `nuclear_reactor_side.png` | 16×16 | Reaktor-Seiten |
| `nuclear_reactor_front.png` | 16×16 | Vorderseite (aus) |
| `nuclear_reactor_front_on.png` | 16×16 | Vorderseite (in Betrieb, leuchtend) |
| `nuclear_reactor_gui.png` | **176×166** | GUI-Hintergrund des Reaktor-Menüs |

---

## ✅ Checkliste — was du JETZT machen sollst (Priorität)

1. [ ] `raw_uranium.png` (16×16)
2. [ ] `uranium_ingot.png` (16×16)
3. [ ] `fuel_rod.png` (16×16)
4. [ ] `uranium_ore.png` (16×16)
5. [ ] `deepslate_uranium_ore.png` (16×16)
6. [ ] `icon.png` (512×512) — optional, kann warten

**Ablage:** alles nach `briefkasten/eingang/` — Namen **exakt** wie oben.

---

## 📌 Wohin ich sie einbaue (zur Info, machst du nicht selbst)

```
raw_uranium.png            → src/main/resources/assets/akw/textures/item/
uranium_ingot.png          → src/main/resources/assets/akw/textures/item/
fuel_rod.png               → src/main/resources/assets/akw/textures/item/
uranium_ore.png            → src/main/resources/assets/akw/textures/block/
deepslate_uranium_ore.png  → src/main/resources/assets/akw/textures/block/
icon.png                   → src/main/resources/assets/akw/  (+ Eintrag in fabric.mod.json)
```

---

## 🛠️ Womit malen?

- **Empfohlen:** [Aseprite](https://www.aseprite.org/) (Pixel-Art, kostenpflichtig)
  oder kostenlos: [Piskel](https://www.piskelapp.com/) (Browser),
  [GIMP](https://www.gimp.org/), [Krita](https://krita.org/).
- Wichtig: Bei 16×16 arbeiten, **Bilinear/Interpolation aus**, als PNG mit
  Alphakanal exportieren.

Fragen oder unklare Specs? Schreib's mir — ich passe die Anweisung an.
