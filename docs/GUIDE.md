# Spieler-Guide

So baust du in der AKW-Mod (Minecraft 1.21.1) dein eigenes Atomkraftwerk — von der
ersten Spitzhacke bis zum FE-Stromnetz. Technische Details siehe
[ARCHITECTURE.md](ARCHITECTURE.md).

---

## 1. Progression auf einen Blick

```
Uranerz abbauen → Roh-Uran → (schmelzen) → Uran-Barren → Brennstab → Reaktor → FE-Strom
```

1. **Uranerz finden & abbauen.** `Uranerz` und `Tiefenschiefer-Uranerz` spawnen untertage
   (Höhe **y −64 … 32**) in allen Overworld-Biomen. Du brauchst mindestens eine
   **Eisen-Spitzhacke**. Abbau dropt **Roh-Uran** (Glück/Fortune erhöht den Drop;
   Behutsamkeit/Silk Touch dropt den Erz-Block selbst).
2. **Roh-Uran schmelzen** im Ofen oder Schmelzofen → **Uran-Barren**.
3. **Brennstab craften** (3 Uran-Barren übereinander).
4. **Reaktor craften & platzieren** (siehe Rezepte unten).
5. **Brennstab einlegen** → der Reaktor erzeugt Strom und gibt ihn an angrenzende
   FE-Speicher/-Maschinen ab.

---

## 2. Reaktor bedienen

- **Rechtsklick** auf den Reaktor öffnet das GUI.
- Lege einen **Brennstab** in den Brennstoff-Slot. Der Reaktor verbrennt ihn nach und
  nach und erzeugt dabei Energie.
- Der **grüne Balken** rechts zeigt den Energiestand (Tooltip beim Überfahren: aktuelle
  Energie / Kapazität in **FE**). Die **orange Anzeige** zeigt den Brennfortschritt des
  aktuellen Stabs.
- Läuft der Reaktor, **leuchtet die Vorderseite**.
- Der Reaktor **gibt Strom über alle 6 Seiten** ab — schließe FE-Kabel oder -Maschinen
  (z. B. aus Tech-Reborn-/FE-kompatiblen Mods) direkt an.
- Wird der Reaktor abgebaut, fällt der eingelegte Brennstab wieder heraus.

> Ein voller Energiespeicher pausiert den Verbrauch — es wird erst wieder ein Brennstab
> gezündet, wenn Platz für neue Energie ist. So verschwendest du keinen Brennstoff.

---

## 3. Die 6 Reaktor-Typen

| Reaktor | Kapazität (FE) | FE/Tick | Abgabe/Tick | Brenndauer je Stab |
|---|--:|--:|--:|--:|
| Reaktor | 100 000 | 40 | 512 | 1600 t |
| Fortgeschrittener Reaktor | 400 000 | 120 | 2 048 | 2000 t |
| Brutreaktor | 800 000 | 240 | 4 096 | 2200 t |
| Thorium-Reaktor | 600 000 | 180 | 3 072 | 2600 t |
| Elite-Reaktor | 1 600 000 | 360 | 8 192 | 2400 t |
| Fusionsreaktor | 4 000 000 | 1 000 | 32 768 | 1200 t |

*(20 Ticks = 1 Sekunde. „FE/Tick" ist die Erzeugung, „Abgabe/Tick" das Maximum, das pro
Seite und Tick weitergegeben wird.)*

---

## 4. Rezepte

**Grundkette**

| Ergebnis | Muster | Zutaten |
|---|---|---|
| Uran-Barren | Schmelzen/Schmelzofen | Roh-Uran |
| Brennstab | 3× senkrecht | Uran-Barren |

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
| Reaktorkern | `UUU` / `UFU` / `UUU` | U=Uran-Barren, F=Brennstab | 1 |
| Steuerstab-Block | `IUI` / `IUI` / `IUI` | I=Eisenbarren, U=Uran-Barren | 1 |
| Kühlrohr | `C C` / `C C` / `C C` | C=Kupferbarren | 2 |
| Blei-Block | `IUI` / `UIU` / `IUI` | I=Eisenbarren, U=Uran-Barren | 1 |
| Abfallbehälter | `IFI` / `IFI` / `III` | I=Eisenbarren, F=Brennstab | 1 |
| Angereicherter-Uran-Block | `UUU` / `UUU` / `UUU` | U=Uran-Barren | 1 |

> Tipp zur Reihenfolge: Erst einen einfachen **Reaktor**, daraus über **Steuerstab-Block**
> und **Kühlrohr** die höheren Tiers. Der **Fusionsreaktor** setzt einen fertigen
> **Elite-Reaktor** voraus.

---

## 5. Reaktor-Bausteine

Diese Blöcke dienen aktuell als Crafting-Bauteile und Deko/Lager (eigene Funktionslogik
wie Kühlung/Strahlung folgt in späteren Versionen):

- **Reaktorkern**, **Steuerstab-Block**, **Kühlrohr** — Bauteile der höheren Reaktoren.
- **Blei-Block** — als Strahlenschutz gedacht, derzeit dekorativ/Lager.
- **Abfallbehälter** — Lager für „verbrauchtes" Material (dekorativ).
- **Angereicherter-Uran-Block** — kompaktes Uran-Lager (leuchtet schwach).

---

## 6. Strom nutzen

Der erzeugte Strom ist **FE** (Forge Energy), bereitgestellt über die *Team Reborn
Energy*-API. Damit ist er kompatibel mit FE-Kabeln, -Akkus und -Maschinen gängiger
Tech-Mods (z. B. Tech Reborn). Platziere einen Verbraucher oder ein Kabel direkt an eine
beliebige Reaktorseite — der Transfer startet automatisch, solange Energie vorhanden ist.
