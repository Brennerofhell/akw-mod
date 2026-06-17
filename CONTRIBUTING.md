# Contributing — AKW Mod

Danke für dein Interesse am Beitragen! Hier steht alles, was du zum lokalen
Entwickeln, Testen und Einreichen von Änderungen brauchst.

---

## Dev-Umgebung einrichten

**Voraussetzungen**

| Tool | Version |
|---|---|
| JDK | 21 (via Homebrew: `openjdk@21`) |
| Python | 3.x (stdlib, kein pip nötig) |
| Git | beliebig |

**Klonen & bauen**

```bash
git clone <repo-url>
cd "akw mod"

# macOS: JAVA_HOME setzen (gradlew findet JDK sonst nicht)
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home

./gradlew build          # → build/libs/akw-*.jar
./gradlew runClient      # startet Minecraft mit dem Mod
```

---

## Neuen Reaktor-Typ hinzufügen

Reaktor-Typen sind datengetrieben — ändere sie **genau an zwei Stellen**:

### 1. `tools/akw_data.py`

Neuen Eintrag in `REACTOR_TYPES` hinzufügen:

```python
{
    "id":       "my_reactor",
    "de":       "Mein Reaktor",
    "en":       "My Reactor",
    "metal":    (R, G, B),       # Grundfarbe der Textur (0–255 je Kanal)
    "accent":   (R, G, B),       # Leucht-/Akzentfarbe
    "capacity": 500_000,         # Energiespeicher in FE
    "gen":      150,             # FE pro Tick erzeugt
    "extract":  3_000,           # max. FE pro Tick abgegeben
    "burn":     2_000,           # Ticks, die ein Brennstab brennt
    "recipe": {
        "pattern": ["AAA", "ANA", "AAA"],
        "key": {
            "A": "minecraft:iron_ingot",
            "N": "akw:nuclear_reactor",   # bestehenden Reaktor als Basis nutzen
        },
    },
},
```

### 2. Generatoren ausführen

```bash
python3 tools/gen_textures.py && python3 tools/gen_resources.py
```

Das erzeugt automatisch Texturen, Blockstates, Modelle, Loot-Tables, Rezepte
und Lang-Einträge.

### 3. `registry/ModBlocks.java`

Feld ergänzen — Werte **identisch** zu `akw_data.py`:

```java
public static final Block MY_REACTOR =
        registerReactor("my_reactor", 500_000, 150, 3_000, 2_000);
```

Der neue Block landet automatisch in `REACTORS`, wird vom BlockEntity-Typ
erkannt, im Energie-Lookup registriert und im Kreativ-Tab angezeigt.

### 4. Prüfen

```bash
./gradlew build
# Headless-Daten-Validierung (prüft Rezepte/Loot ohne GUI):
( printf 'stop\n' | perl -e 'alarm shift; exec @ARGV' 300 \
    ./gradlew runServer --console=plain > /tmp/akw_server.log 2>&1 )
grep -iE "error|exception|fail" /tmp/akw_server.log
```

---

## Neuen Baustein hinzufügen

Analog zu Reaktoren, aber in `DECOR_BLOCKS` (`akw_data.py`) und
`registerDecor(...)` (`ModBlocks.java`). Bausteine sind aktuell einfache
Vollwürfel ohne Tick-Logik.

---

## Code-Konventionen

- **Sprache in Code, Logs, Kommentaren:** Deutsch.
- **Mod-ID:** `akw` — immer via `Identifier.of(AkwMod.MOD_ID, name)`.
- **Registrierungsreihenfolge beachten:**
  `Items → Blocks → BlockEntities → ScreenHandlers → ItemGroups → WorldGen → EnergyLookup`.
  `ModBlockEntities` braucht die fertige `ModBlocks.REACTORS`-Liste.
- **Keine neuen Tier-Werte in der BlockEntity** — Kapazität, Gen, Extract und
  Burn werden aus dem `NuclearReactorBlock`-Objekt gelesen. So teilen sich alle
  Tiers eine einzige BlockEntity-Klasse.
- Keine unnötigen Kommentare — selbst erklärende Namen bevorzugen.

---

## Pull Requests

1. Branch von `main` erstellen (`feature/<name>` oder `fix/<name>`).
2. Änderungen klein halten — ein Feature/Fix pro PR.
3. Beschreibe im PR-Body kurz **warum** (nicht nur was).
4. Wenn du Reaktor-Werte änderst: gib den Grund für das Balancing an.

---

## Bekannte Fallstricke

| Problem | Ursache | Lösung |
|---|---|---|
| `gradlew` meldet „Unable to locate Java Runtime" | `JAVA_HOME` nicht gesetzt | Siehe oben |
| `No key layers in MapLike[{}]` im Server-Log | Harmloser Vanilla-Fehler | Ignorieren |
| Rezept-Fehler „expected item" | `"result": {"item": ...}` statt `"id"` | `gen_resources.py` nutzt bereits `"id"` — nicht manuell editieren |
| Textur fehlt nach neuem Reaktor | Generatoren nicht ausgeführt | `python3 tools/gen_textures.py && python3 tools/gen_resources.py` |
