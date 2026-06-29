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

### Steuerung im GUI: Redstone- & Komparator-Modus

Links im Reaktor-GUI gibt es zwei Knöpfe (gelten für Einblock- **und** Multiblock-Reaktor):

**Redstone-Modus** — wie der Reaktor auf ein Redstone-Signal reagiert:

| Modus | Verhalten |
|---|---|
| Ignoriert | Redstone hat keinen Einfluss |
| Signal aktiviert | Reaktor zündet nur bei anliegendem Signal |
| Signal deaktiviert *(Standard)* | Reaktor zündet nur ohne Signal; laufender Stab brennt ab |
| Not-Aus (SCRAM) | wie „Signal deaktiviert", aber ein Signal stoppt den laufenden Stab **sofort** |

**Komparator-Modus** — was der Komparator-Ausgang (0–15) misst:

| Modus | Signalquelle |
|---|---|
| Energie *(Standard)* | Energiefüllstand |
| Temperatur | Hitze / maxHitze |
| Brennstoff | Brennstoff-Slot |
| Abfall | Abfall-Slot |

> Beispiel-Automation: Komparator im Modus „Temperatur" → Signal bei Überhitzung →
> Redstone-Leitung → benachbarter Reaktor im Modus „Signal deaktiviert" pausiert.

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

## 5. Modularer Multiblock-Reaktor

Neben den sechs fertigen Einblockreaktoren gibt es eine zweite Bauweise: den modularen
Reaktor. Seine Leistung hängt von den wirklich eingebauten Modulen ab.

### Hülle bauen

1. Baue einen hohlen Würfel mit Außenmaß **3×3×3**, **5×5×5** oder **7×7×7**.
2. Die Außenwand muss vollständig aus **Reaktor-Gehäusen** bestehen.
3. Ersetze den mittleren Block einer Seitenwand durch genau einen
   **Multiblock-Controller**. Der Innenraum liegt hinter seiner Vorderseite.
4. Setze mindestens einen **Reaktorkern** in den Innenraum.
5. Erlaubt sind innen Luft, Reaktorkerne, Steuerstäbe, Kühlrohre und Blei-Blöcke.
6. Rechtsklicke den Controller mit dem **Reaktor-Schraubenschlüssel**.

Der Controller meldet, ob die Hülle unvollständig ist, ein Kern fehlt oder ein
ungültiger Block im Innenraum steht. Er prüft die Struktur während des Betriebs alle
100 Ticks erneut.

### Innenmodule

- Jeder **Reaktorkern** kann einen Brennstab pro Zyklus verwenden und liefert bis zu
  **80 FE/Tick**.
- Direkt benachbarte Kerne erhöhen ihre Reaktivität und damit die Leistung, erzeugen
  aber konzentrierte Wärme.
- Bis zu zwei angrenzende **Steuerstäbe** senken die Wärme eines Kerns.
- Ein **Kühlrohr** wirkt nur, wenn sein zusammenhängendes Rohrnetz die Außenhülle
  berührt. Der Kern muss direkt an ein solches Netz angrenzen.
- Nicht mit der Hülle verbundene Kühlrohre werden beim Assemblieren angezeigt, kühlen
  den Reaktor aber nicht.

Ein Brennzyklus aktiviert so viele Kerne, wie Brennstäbe und freie Plätze im
Abfallslot vorhanden sind. Pro aktivem Kern werden ein Brennstab verbraucht und ein
verbrauchter Brennstab erzeugt. Bei 90 Prozent Maximaltemperatur erfolgt eine
automatische Abschaltung; bei weiterem Temperaturanstieg bleibt die Kernschmelze
gefährlich.

### Anschlüsse im aktuellen MVP

- Rechtsklick auf den assemblierten Controller öffnet das bekannte Reaktor-GUI.
- Hopper von oben liefern Brennstäbe; Hopper von unten entnehmen Abfall.
- FE wird über angrenzende Kabel, Akkus oder Verbraucher ausgegeben.
- Ein Redstone-Signal am Controller verhindert den Start eines neuen Brennzyklus.

Eigene Energie-, Item- und Redstone-Ports sowie rechteckige Hüllen bis 9×9×9 sind für
die nächste Multiblock-Ausbaustufe vorgesehen.

### Bauroboter (automatischer Aufbau)

Wer den 3×3×3-Multiblock nicht von Hand bauen will, nutzt den **Bauroboter-Controller**
(`akw:reactor_builder_controller`). Er baut die komplette Struktur (Gehäuse, Kern, Controller)
automatisch aus seinem Inventar zusammen.

1. Platziere den Bauroboter-Controller. Vor seiner Vorderseite entsteht der Reaktor.
2. **Rechtsklick** öffnet das 27-Slot-Inventar — fülle es mit **Reaktor-Gehäusen**,
   **Reaktorkern** und einem **Multiblock-Controller**.
3. Lade ihn mit **Energie** (Kabel/Akku an den Controller; **500 FE pro gesetztem Block**).
4. **Shift-Rechtsklick** startet bzw. pausiert den Bau. Ein sichtbarer Roboter setzt alle
   5 Ticks einen Block.
5. Der Status erscheint in der Actionbar: fehlendes Material, Blockade, zu wenig Energie oder
   „fertig". Ist die Struktur gültig, ist der Multiblock-Reaktor sofort assembliert.

> Ein Redstone-Signal am Bauroboter **pausiert** den Bau. Der Komparator-Ausgang zeigt den
> Baufortschritt (0–15). Beim Abbau wirft der Controller Inventar und Roboter wieder aus.

---

## 6. Energie-Infrastruktur

### Energie-Kabel (`akw:energy_cable`)
- Transportiert FE zwischen Blöcken.
- Puffer: **8 192 FE**, Transfer: **2 048 FE/Tick** (pro Seite).
- Mehrere Kabel hintereinander → Strom fließt von Erzeugern zu Speichern/Verbrauchern.
- Crafting: `CRC` (C = Kupferbarren, R = Redstone) → **3 Kabel**

### Akku-Block (`akw:energy_battery`)
- Großer FE-Puffer: **1 000 000 FE**, Transfer: **4 096 FE/Tick**.
- Gibt den **Füllstand als Redstone-Signal** (0–15) aus (Komparator direkt daneben).
- Crafting: `ICI / RRR / ICI` (I = Eisenbarren, C = Kupferbarren, R = Redstone)

**FE-Kompatibilität:** Beide Blöcke nutzen das **NeoForge-eigene Energiesystem**
(`Capabilities.Energy` / FE) und sind damit automatisch mit allen FE-kompatiblen Mods
verbunden (kein extra Addon nötig).

---

## 7. Rezepte

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

## 8. Reaktor-Bausteine

- **Reaktorkern**, **Steuerstab-Block** — Innenmodule des Multiblocks und
  Crafting-Bauteile der höheren Einblockreaktoren.
- **Kühlrohr** — direkte Kühlung bei Einblockreaktoren; verbundenes Wärmenetz im Multiblock.
- **Blei-Block** — funktionaler Strahlenschutz: ein Blei-Block auf der direkten Linie zwischen
  Strahlungsquelle (Reaktor/Abfallbehälter) und Spieler blockt die Strahlung vollständig.
- **Abfallbehälter** — vollwertiger 9-Slot-Speicher für Verbrauchte Brennstäbe (Rechtsklick öffnet
  das GUI). Komparator-Ausgang 0–15; Hopper oben/seitlich einlagern, unten entnehmen; strahlt bei
  Füllstand > 50 % schwach (Stufe 0, 5-Block-Radius).
- **Angereicherter-Uran-Block** — kompaktes Uran-Lager (leuchtet schwach).

---

## 9. Strom nutzen

Der erzeugte Strom ist **FE** (Forge Energy), bereitgestellt über das **NeoForge-eigene
Energiesystem** (`Capabilities.Energy`). Platziere einen Verbraucher, ein **Energie-Kabel**
oder einen **Akku-Block** direkt an eine beliebige Reaktorseite — der Transfer startet
automatisch. Kompatibel mit FE-Maschinen anderer NeoForge-Tech-Mods (kein extra Addon nötig).
