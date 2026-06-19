# Spieler-Guide

So baust du in der AKW-Mod (Minecraft 1.21.10) dein eigenes Atomkraftwerk — von der
ersten Spitzhacke bis zum FE-Stromnetz. Technische Details siehe
[ARCHITECTURE.md](ARCHITECTURE.md).

---

## 1. Progression auf einen Blick

```
Uranerz abbauen → Roh-Uran → (schmelzen) → Uran-Barren → Brennstab
    → Reaktor + Kühlrohre → FE-Strom → Kabel → Akku-Block → Verbraucher
```

1. **Uranerz finden & abbauen.** `Uranerz` und `Tiefenschiefer-Uranerz` spawnen untertage
   (Höhe **y −64 … 32**) in allen Overworld-Biomen. Du brauchst mindestens eine
   **Eisen-Spitzhacke**. Abbau dropt **Roh-Uran** (Glück/Fortune erhöht den Drop;
   Behutsamkeit/Silk Touch dropt den Erz-Block selbst).
2. **Roh-Uran schmelzen** im Ofen oder Schmelzofen → **Uran-Barren**.
3. **Brennstab craften** (3 Uran-Barren übereinander).
4. **Reaktor craften & platzieren** (siehe Rezepte unten).
5. **Kühlrohre direkt neben den Reaktor setzen** — jedes Rohr kühlt 8 Hitze/Tick;
   ohne Kühlung explodiert der Reaktor bei maximaler Hitze.
6. **Brennstab einlegen** → der Reaktor erzeugt Strom.
7. **Kabel** transportieren den Strom weiter, der **Akku-Block** speichert ihn.

---

## 2. Reaktor bedienen

- **Rechtsklick** auf den Reaktor öffnet das GUI.
- Lege einen **Brennstab** in den Brennstoff-Slot. Der Reaktor verbrennt ihn nach und
  nach und erzeugt dabei Energie.
- Der **grüne Balken** rechts zeigt den Energiestand (Tooltip: `FE / Kapazität`).
  Die **orange Anzeige** zeigt den Brennfortschritt des aktuellen Stabs.
- Der **Hitzebalken** zeigt die aktuelle Hitze:
  - **orange:** normaler Betrieb.
  - **rot (ab 75 % maxHitze):** Drosselung — Leistung fällt auf 25 %.
  - **100 % maxHitze:** Reaktor explodiert (Stärke je nach Typ).
- Läuft der Reaktor, **leuchtet die Vorderseite**.
- Der Reaktor **gibt Strom über alle 6 Seiten** ab.
- Wird der Reaktor abgebaut, fällt der eingelegte Brennstab wieder heraus.

> Ein voller Energiespeicher pausiert den Verbrauch — erst wenn wieder Platz ist,
> wird ein neuer Brennstab gezündet. So verschwendest du keinen Brennstoff.

---

## 3. Kühlsystem

Reaktoren erzeugen pro Betrieb-Tick Hitze. Ohne Kühlung steigt sie bis zur Explosion.

**Kühlung:**
- **Passiv:** jeder Reaktor kühlt sich um **2 Hitze/Tick** selbst.
- **Kühlrohr:** jedes direkt angrenzende Kühlrohr senkt die Hitze um weitere **8 Hitze/Tick**.

**Mindestrohre für stabilen Betrieb:**

| Reaktor | Hitze/Tick | benötigte Kühlrohre |
|---|--:|--:|
| Reaktor | 6 | 1 (deckt 10/Tick) |
| Fortgeschrittener Reaktor | 14 | 2 (deckt 18/Tick) |
| Brutreaktor | 18 | 2 (exakt) |
| Thorium-Reaktor | 16 | 2 (deckt 18/Tick) |
| Elite-Reaktor | 28 | 4 (deckt 34/Tick) |
| Fusionsreaktor | 44 | 6 (alle Seiten, deckt 50/Tick) |

> Kühlrohre können an alle 6 Seiten gestellt werden — auch unter oder über den Reaktor.

---

## 4. Die 6 Reaktor-Typen

| Reaktor | Kapazität | FE/Tick | Abgabe/Tick | Brenndauer | maxHitze | Hitze/Tick |
|---|--:|--:|--:|--:|--:|--:|
| Reaktor | 100 000 | 40 | 512 | 1600 t | 1200 | 6 |
| Fortgeschrittener Reaktor | 400 000 | 120 | 2 048 | 2000 t | 2000 | 14 |
| Brutreaktor | 800 000 | 240 | 4 096 | 2200 t | 2400 | 18 |
| Thorium-Reaktor | 600 000 | 180 | 3 072 | 2600 t | 2200 | 16 |
| Elite-Reaktor | 1 600 000 | 360 | 8 192 | 2400 t | 3200 | 28 |
| Fusionsreaktor | 4 000 000 | 1 000 | 32 768 | 1200 t | 4000 | 44 |

*(20 Ticks = 1 Sekunde.)*

---

## 5. Energie-Infrastruktur

### Energie-Kabel (`akw:energy_cable`)
- Transportiert FE zwischen Blöcken.
- Puffer: **8 192 FE**, Transfer: **2 048 FE/Tick** (pro Seite).
- Mehrere Kabel hintereinander → Strom fließt von Erzeugern zu Speichern/Verbrauchern.
- Crafting: `CRC` (C = Kupferbarren, R = Redstone) → **3 Kabel**

### Akku-Block (`akw:energy_battery`)
- Großer FE-Puffer: **1 000 000 FE**, Transfer: **4 096 FE/Tick**.
- Gibt den **Füllstand als Redstone-Signal** (0–15) aus (Komparator direkt daneben).
- Crafting: `ICI / RRR / ICI` (I = Eisenbarren, C = Kupferbarren, R = Redstone)

**Create-Kompatibilität:** Beide Blöcke exponieren `EnergyStorage.SIDED` → automatisch
kompatibel mit Create-FE-Brücken (kein extra Addon nötig).

---

## 6. Rezepte

**Grundkette**

| Ergebnis | Muster | Zutaten |
|---|---|---|
| Uran-Barren | Schmelzen/Schmelzofen | Roh-Uran |
| Brennstab | `I` / `I` / `I` (senkrecht) | I = Uran-Barren |

**Reaktoren** (3×3-Werkbank; höhere Tiers verbauen den jeweils kleineren Reaktor):

| Reaktor | Muster | Schlüssel |
|---|---|---|
| Reaktor | `III` / `UFU` / `IRI` | I=Eisenbarren, U=Uran-Barren, F=Ofen, R=Redstone |
| Fortgeschritten | `GCG` / `CNC` / `GCG` | G=Goldbarren, C=Steuerstab-Block, N=Reaktor |
| Elite | `DCD` / `CAC` / `DCD` | D=Diamant, C=Kühlrohr, A=Fortgeschr. Reaktor |
| Brutreaktor | `CCC` / `UNU` / `CCC` | C=Kupferbarren, U=Angereicherter-Uran-Block, N=Reaktor |
| Thorium | `MEM` / `ENE` / `MEM` | M=Smaragd, E=Angereicherter-Uran-Block, N=Reaktor |
| Fusion | `NDN` / `DED` / `NDN` | N=Netheritbarren, D=Diamantblock, E=Elite-Reaktor |

**Bausteine**

| Block | Muster | Schlüssel | Ausbeute |
|---|---|---|--:|
| Reaktorkern | `UUU` / `UFU` / `UUU` | U = Uran-Barren, F = Brennstab | 1 |
| Steuerstab-Block | `IUI` / `IUI` / `IUI` | I = Eisenbarren, U = Uran-Barren | 1 |
| Kühlrohr | `C C` / `C C` / `C C` | C = Kupferbarren | 2 |
| Blei-Block | `IUI` / `UIU` / `IUI` | I = Eisenbarren, U = Uran-Barren | 1 |
| Abfallbehälter | `IFI` / `IFI` / `III` | I = Eisenbarren, F = Brennstab | 1 |
| Angereicherter-Uran-Block | `UUU` / `UUU` / `UUU` | U = Uran-Barren | 1 |

**Energie-Infrastruktur**

| Block | Muster | Schlüssel | Ausbeute |
|---|---|---|--:|
| Energie-Kabel | `CRC` | C = Kupferbarren, R = Redstone | 3 |
| Akku-Block | `ICI` / `RRR` / `ICI` | I = Eisenbarren, C = Kupferbarren, R = Redstone | 1 |

> Tipp: Erst einen einfachen **Reaktor** + **Kühlrohre**, dann höhere Tiers.
> Der **Fusionsreaktor** setzt einen fertigen **Elite-Reaktor** voraus.

---

## 7. Reaktor-Bausteine

- **Reaktorkern**, **Steuerstab-Block** — Crafting-Bauteile der höheren Reaktoren.
- **Kühlrohr** — aktive Kühlung (−8 Hitze/Tick je angrenzendem Rohr).
- **Blei-Block** — als Strahlenschutz gedacht (derzeit dekorativ/Lager).
- **Abfallbehälter** — Lager für verbrauchtes Material (dekorativ).
- **Angereicherter-Uran-Block** — kompaktes Uran-Lager (leuchtet schwach).

---

## 8. Strom nutzen

Der erzeugte Strom ist **FE** (Forge Energy), bereitgestellt über die *Team Reborn
Energy*-API. Platziere einen Verbraucher, ein **Energie-Kabel** oder einen **Akku-Block**
direkt an eine beliebige Reaktorseite — der Transfer startet automatisch.
Kompatibel mit FE-Maschinen aus Tech Reborn, Create (via FE-Brücke) und anderen Tech-Mods.
