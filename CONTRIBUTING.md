# Contributing — AKW Mod

Danke für dein Interesse am Beitragen! Hier steht alles, was du zum lokalen
Entwickeln, Testen und Einreichen von Änderungen brauchst.

---

## Dev-Umgebung einrichten

**Voraussetzungen**

| Tool | Version |
|---|---|
| JDK | 21 (via Homebrew: `openjdk@21`) |
| NeoForge | 21.10.64 (zieht Gradle/moddev automatisch) |
| Git | beliebig |

> Assets werden vollständig per **Java-Datagen** (NeoForge `GatherDataEvent`) erzeugt — die früheren
> Python-Generatoren (`tools/gen_*.py`) werden nicht mehr benötigt.

**Klonen & bauen**

```bash
git clone <repo-url>
cd "akw mod"

# macOS: JAVA_HOME setzen (gradlew findet JDK sonst nicht)
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home

./gradlew build          # → build/libs/neoforge/akw-*.jar
./gradlew runClient      # startet Minecraft mit dem Mod
```

---

## Neuen Reaktor-Typ hinzufügen

Alle Ressourcen werden per **NeoForge Datagen** (`GatherDataEvent`, Java) erzeugt — kein Python, kein
manuelles JSON-Schreiben.

### 1. `registry/ModBlocks.java` — Block registrieren

```java
public static final DeferredBlock<NuclearReactorBlock> MY_REACTOR =
        registerReactor("my_reactor", 500_000, 150, 3_000, 2_000, 2_400, 16);
//                        id           FE-cap  gen  extract burn  maxHeat heat/t
```

Der neue Block landet automatisch in `REACTORS`, wird vom BlockEntity-Typ
erkannt, in der Energie-Capability registriert und im Kreativ-Tab angezeigt.

### 2. `datagen/ModRecipeProvider.java` — Rezept ergänzen

Innerhalb von `buildRecipes()` (im inneren `RecipeProvider`):

```java
shaped(RecipeCategory.MISC, ModBlocks.MY_REACTOR)
        .pattern("AAA").pattern("ANA").pattern("AAA")
        .define('A', Items.IRON_INGOT)
        .define('N', ModBlocks.NUCLEAR_REACTOR)
        .unlockedBy(getHasName(ModBlocks.NUCLEAR_REACTOR), has(ModBlocks.NUCLEAR_REACTOR))
        .save(output, key("my_reactor"));
```

### 3. `datagen/ModLanguageProvider.java` — Namen ergänzen

```java
// In German.addTranslations:
addBlock(ModBlocks.MY_REACTOR, "Mein Reaktor");
// In English.addTranslations:
addBlock(ModBlocks.MY_REACTOR, "My Reactor");
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
jeden Block in `ModBlocks.REACTORS` (via `createFurnace`) — du musst dort nichts ändern.
In `ModLootTableProvider` muss der Block zusätzlich in `getKnownBlocks()` aufgenommen werden.

### 5. Datagen ausführen

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew runServerData
cp -r src/main/generated/data /tmp/akw_data        # data/ sichern (Purge-Schutz)
./gradlew runClientData
rm -rf src/main/generated/data && cp -r /tmp/akw_data src/main/generated/data
```

Das schreibt alle JSONs (Blockstate, Modelle, Loot-Table, Rezept, Tags, Lang, Advancements)
nach `src/main/generated/`. Hintergrund zum `data/`-Purge: [docs/NEOFORGE-MIGRATION.md](docs/NEOFORGE-MIGRATION.md) §6.

### 6. Prüfen

```bash
./gradlew build
```

---

## Neuen Baustein hinzufügen

1. `registry/ModBlocks.java` — `registerDecor("my_block", Block::new, settings)` ergänzen.
2. `datagen/ModLootTableProvider.java` — `dropSelf(ModBlocks.MY_BLOCK.get())` **und** den Block in
   `getKnownBlocks()` ergänzen.
3. `datagen/ModModelProvider.java` — `blockModels.createTrivialCube(ModBlocks.MY_BLOCK.get())` ergänzen.
4. `datagen/ModLanguageProvider.java` — Namen in DE und EN ergänzen; Textur unter
   `src/main/resources/assets/akw/textures/block/my_block.png` anlegen.
5. `./gradlew runClientData && runServerData && ./gradlew build`

Bausteine sind einfache Vollwürfel (`createTrivialCube`) — kein Blockstate- oder Modell-Code nötig.

---

## Code-Konventionen

- **Sprache in Code, Logs, Kommentaren:** Deutsch.
- **Mod-ID:** `akw` — immer via `ResourceLocation.fromNamespaceAndPath(AkwMod.MOD_ID, name)`.
- **Registrierungsreihenfolge beachten** (im `AkwMod`-Konstruktor):
  `Items → Blocks → Effects → Sounds → BlockEntities → ScreenHandlers → ItemGroups`,
  dazu `registerCapabilities` als `RegisterCapabilitiesEvent`-Listener.
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
| Beim Mod-Laden `NullPointerException: Item id not set` | BlockItem ohne ID registriert | `ITEMS.registerSimpleBlockItem(name, block)` statt `new BlockItem(...)` |
| `data/`-Assets verschwinden nach Datagen | `runClientData`/`runServerData` purgen sich gegenseitig | `data/` zwischen den Läufen sichern (siehe „Datagen ausführen") |
| JSON-Dateien direkt editiert, Build überschreibt sie | `src/main/generated/` ist Datagen-Ausgabe | Quelle ist Java-Datagen, nicht die JSONs |
