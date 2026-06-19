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

Alle Ressourcen werden per **Fabric Datagen** (Java) erzeugt — kein Python, kein
manuelles JSON-Schreiben.

### 1. `registry/ModBlocks.java` — Block registrieren

```java
public static final Block MY_REACTOR =
        registerReactor("my_reactor", 500_000, 150, 3_000, 2_000);
//                        id           FE-cap  gen  extract burn
```

Der neue Block landet automatisch in `REACTORS`, wird vom BlockEntity-Typ
erkannt, im Energie-Lookup registriert und im Kreativ-Tab angezeigt.

### 2. `datagen/ModRecipeProvider.java` — Rezept ergänzen

```java
ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.MY_REACTOR)
        .pattern("AAA").pattern("ANA").pattern("AAA")
        .input('A', Items.IRON_INGOT)
        .input('N', ModBlocks.NUCLEAR_REACTOR)
        .criterion(hasItem(ModBlocks.NUCLEAR_REACTOR),
                   conditionsFromItem(ModBlocks.NUCLEAR_REACTOR))
        .offerTo(exporter, Identifier.of("akw", "my_reactor"));
```

### 3. `datagen/ModLanguageProvider.java` — Namen ergänzen

```java
// In German.generateTranslations:
builder.add(ModBlocks.MY_REACTOR, "Mein Reaktor");
// In English.generateTranslations:
builder.add(ModBlocks.MY_REACTOR, "My Reactor");
```

### 4. Texturen anlegen

Lege diese PNG-Dateien unter `src/main/resources/assets/akw/textures/block/` an
(16×16 px):

| Dateiname | Verwendung |
|---|---|
| `my_reactor_top.png` | Ober-/Unterseite |
| `my_reactor_front.png` | Vorderseite (aus) |
| `my_reactor_front_on.png` | Vorderseite (an, leuchtet) |
| `my_reactor_side.png` | Die vier übrigen Seiten |

Die `ModModelProvider`-Klasse generiert Blockstate und Modell automatisch für
jeden Block in `ModBlocks.REACTORS` — du musst dort nichts ändern.

### 5. Datagen ausführen

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew runDatagen
```

Das schreibt alle JSONs (Blockstate, Modelle, Loot-Table, Rezept, Tags, Lang)
direkt nach `src/main/resources/`.

### 6. Prüfen

```bash
./gradlew build
```

---

## Neuen Baustein hinzufügen

1. `registry/ModBlocks.java` — `registerDecor("my_block", settings)` ergänzen.
2. `datagen/ModLootTableProvider.java` — `addDrop(ModBlocks.MY_BLOCK)` ergänzen.
3. `datagen/ModLanguageProvider.java` — Namen in DE und EN ergänzen.
4. `src/main/resources/assets/akw/textures/block/my_block.png` anlegen.
5. `./gradlew runDatagen && ./gradlew build`

Bausteine sind einfache Vollwürfel (`registerSimpleCubeAll`) — kein Blockstate-
oder Modell-Code nötig.

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

## Branch-Strategie

```
main
├─ feature/<name>   ← neue Features (z. B. feature/radiation-mechanic)
├─ fix/<name>       ← Bugfixes (z. B. fix/energy-overflow)
└─ docs/<name>      ← reine Dokumentations-Änderungen
```

**Regeln:**

| Branch | Zweck | Merge-Ziel |
|---|---|---|
| `main` | Stabiler Stand — immer releasefähig | — |
| `feature/<name>` | Ein Feature pro Branch | `main` via PR |
| `fix/<name>` | Ein Bug pro Branch | `main` via PR |
| `docs/<name>` | Nur Markdown/JavaDoc, kein Java-Code | `main` via PR |

- Direkte Commits auf `main` sind **nicht** erlaubt — immer PR.
- Branch-Namen in **Kleinbuchstaben mit Bindestrich** (`feature/cooling-logic`, nicht `Feature_CoolingLogic`).
- Branch löschen, sobald der PR gemergt ist.

**Wann einen neuen Branch anlegen:**

```bash
git checkout main && git pull
git checkout -b feature/mein-feature
```

**Releases** werden als Git-Tags auf `main` gesetzt (`v0.3.0`, `v1.0.0`, …) — kein eigener Release-Branch nötig, da die Mod nur einen aktiven Entwicklungsstrang hat.

---

## Pull Requests

1. Branch von `main` erstellen (Namensschema siehe oben).
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
