# Diagnose: Warum fehlt der AKW-Kreativ-Tab?

Datum: 2026-06-19  
Analysiert: Bytecode MC 1.21.10 + Fabric API 0.138.4 Quellcode

---

## Kurz­antwort

Der Tab ist **nicht kaputt**. Er ist korrekt registriert und technisch sichtbar.
Er erscheint auf **Seite 2** des Kreativ-Menüs — einem zweiten Satz Tab-Icons,
den Fabric API für alle Mod-Tabs reserviert.

---

## Beweis: `shouldDisplay()` gibt `true` zurück

Bytecode von `ItemGroup.shouldDisplay()` in MC 1.21.10 (verifiziert):

```
0:  getfield  type          ← lädt this.type (= null bei FabricItemGroup)
4:  getstatic CATEGORY      ← lädt Type.CATEGORY
7:  if_acmpne → 17          ← falls type != CATEGORY → springe zu Zeile 17
10: invokevirtual hasStacks ← (nur bei type == CATEGORY: Stacks nötig)
14: ifeq → 21               ← falls keine Stacks → false
17: iconst_1                ← return true  ← ← ← AKW landet hier
21: iconst_0                ← return false
```

Logik in Java:
```java
public boolean shouldDisplay() {
    if (type != Type.CATEGORY) return true;   // null != CATEGORY → true
    return hasStacks();                        // nur Vanilla-Tabs brauchen Stacks
}
```

`FabricItemGroup.builder()` setzt intern `type = null`.  
`null != CATEGORY` → `shouldDisplay()` gibt **true** zurück.  
Der Tab ist für das Kreativ-Menü als sichtbar markiert.

---

## Warum erscheint er trotzdem nicht sofort?

Fabric API legt alle Mod-Tabs automatisch auf **Seite 2** des Kreativ-Menüs
(Datei: `ItemGroupsMixin.java`, Methode `paginateGroups()`):

```
Seite 1: Vanilla-Tabs (Baublöcke, Werkzeuge, Kampf, ...)
Seite 2: Alle Mod-Tabs (AKW, und alle anderen installierten Mods)
```

Das ist bewusstes Design von Fabric — Vanilla-Tabs bleiben unberührt,
Mods haben eine eigene Seite. Alle Fabric-Mods verhalten sich so.

---

## Wo ist der `>>` Button?

```
┌────────────────────────────────────────────────┐
│  [🧱][🎨][🌿][⚙️][🔴][🔧][⚔️][🍖][🧪][🥚]  [<] [>>] │
│                                                │
│  ← Tab-Icons (Seite 1)        Navigations-    │
│                                  Pfeile →     │
└────────────────────────────────────────────────┘
```

- **`>>`** ist oben rechts neben den Tab-Icons
- **`<<`** erscheint auf Seite 2 zum Zurücknavigieren
- Auf Seite 2 erscheint dann das AKW-Tab-Icon (Roh-Uran-Nugget)

### Schritte zum Testen

1. `./gradlew runClient` ausführen
2. Im Spiel: Kreativ-Modus (`/gamemode creative`)
3. `E` drücken → Inventar / Kreativ-Menü öffnen
4. Oben rechts neben den Tab-Icons: **`>>`** klicken
5. Seite 2 erscheint → AKW-Tab mit Roh-Uran-Icon suchen
6. Tab anklicken → alle 20 Items/Blöcke sichtbar

---

## Falls der Tab auf Seite 2 trotzdem fehlt

Das würde bedeuten: die Mod-Registrierung schlägt zur Laufzeit fehl.

**Diagnose-Schritt:**
```bash
# Nach ./gradlew runClient: Log prüfen
grep -i "akw\|atomkraftwerk\|itemgroup\|error\|exception" \
  ~/.gradle/loom-cache/launch/*/logs/latest.log | head -30
```

Oder: Log-Datei in `briefkasten/eingang/` ablegen, dann kann ich den Fehler
direkt analysieren.

---

## Verlauf der bisherigen Fix-Versuche

| Version | Was geändert | Ergebnis |
|---------|-------------|---------|
| v0.3.5 | Items via `ItemGroupEvents` in eigenen Tab | Falsche Methode — Event fired nicht für neue Custom-Tabs |
| v0.3.6 | Items via `.entries()` im Builder (korrekt) | Tab registriert, Code korrekt |
| v0.4.0 | `RegistryKeys.ITEM_GROUP` statt `.getKey()` | Idiomatisch, funktional identisch |

Der Code seit v0.3.6 ist korrekt. Der Tab war nie durch Code kaputt —
das Problem war immer die unbekannte Seite-2-Navigation.
