# Konzept: Modularer Reaktor

Datum: 2026-06-21  
Status: Umsetzungsvorschlag für eine neue Reaktor-Generation

## 1. Ziel

Der bestehende Multiblock-Reaktor soll zu einem echten modularen System werden.
Größe, Leistung, Kühlung, Sicherheit und Automatisierung ergeben sich aus den
verbauten Komponenten. Der Spieler baut damit keinen festen Reaktor-Tier mehr,
sondern entwirft eine eigene Anlage mit nachvollziehbaren Vor- und Nachteilen.

Der modulare Reaktor soll:

- vorhandene Blöcke wie Gehäuse, Reaktorkern, Steuerstab und Kühlrohr wiederverwenden,
- verschiedene rechteckige Baugrößen unterstützen,
- durch die Anordnung der Innenmodule beeinflusst werden,
- Brennstoff, Energie, Kühlmittel und Abfall getrennt behandeln,
- per Redstone und Hopper beziehungsweise Pipes automatisierbar sein,
- Fehler verständlich im Controller-GUI anzeigen,
- die bisherigen festen Einzelblock-Reaktoren weiterhin erlauben.

## 2. Grundaufbau

### Zulässige Struktur

- Außenmaß je Achse: **3 bis 9 Blöcke**.
- Quader statt ausschließlich Würfel, beispielsweise 5×5×7.
- Die Außenhülle muss vollständig aus Reaktorgehäusen oder gültigen Ports bestehen.
- Genau ein Controller sitzt in der Außenhülle.
- Der Innenraum enthält Luft oder gültige Reaktormodule.
- Die Struktur darf keine fremden Blöcke und keine Löcher enthalten.

Der Controller ermittelt beim Assemblieren die gegenüberliegenden Grenzen der
zusammenhängenden Hülle. Dadurch ist kein festes Mittelpunkt-System mehr nötig.
Die maximalen 9×9×9 begrenzen Suchaufwand und gespeicherte Daten.

### Außenblöcke

| Block | Aufgabe |
|---|---|
| `reactor_casing` | Normale, strahlungsfeste Außenhülle |
| `modular_reactor_controller` | Assemblierung, Status, GUI und Tick-Logik |
| `reactor_energy_port` | FE-Ausgabe; Seite im GUI konfigurierbar |
| `reactor_item_port` | Brennstoff-Eingang oder Abfall-Ausgang |
| `reactor_redstone_port` | Aktivierung, Not-Aus und Statussignal |
| `reactor_coolant_port` | Spätere Erweiterung für Flüssigkeitskühlung |

Für den MVP reichen Controller, Energie-Port und Item-Port. Kühlmittel-Port und
erweiterter Redstone-Port können in einer zweiten Ausbaustufe folgen.

### Innenmodule

| Vorhandener Block | Neue Funktion im Multiblock |
|---|---|
| `reactor_core` | Brennelement-Modul; bestimmt Grundproduktion und Brennstoffverbrauch |
| `control_rod_block` | Steuerungsmodul; senkt Leistung und Hitze benachbarter Kerne |
| `cooling_pipe` | Kühlkanal; führt Hitze von benachbarten Kernen ab |
| `lead_block` | Abschirmmodul; senkt austretende Strahlung bei beschädigter Hülle |

Später mögliche Module:

- `heat_exchanger`: verstärkt angrenzende Kühlkanäle,
- `moderator_block`: erhöht Effizienz, aber auch Hitze,
- `emergency_coolant_cell`: einmaliger Überhitzungsschutz,
- `waste_processor`: reduziert Abfallmenge gegen Energieverlust.

## 3. Platzierungsregeln

Die Platzierung soll relevant, aber noch verständlich bleiben:

1. Ein Reaktorkern arbeitet nur, wenn mindestens eine seiner sechs Seiten an Luft
   oder einen Kühlkanal grenzt.
2. Jeder direkt angrenzende Kühlkanal erhöht die Kühlleistung dieses Kerns.
3. Jeder direkt angrenzende Steuerstab reduziert Leistung und Wärme dieses Kerns.
4. Ein Kühlkanal benötigt eine zusammenhängende Verbindung zur Außenhülle oder zu
   einem späteren Wärmetauscher.
5. Vollständig eingeschlossene Kerne gelten als ungekühlt und erhöhen das Risiko.

Beim Assemblieren wird daraus ein unveränderlicher `ReactorLayout` berechnet.
Während des normalen Ticks werden keine kompletten Blockscans durchgeführt.
Alle 100 Ticks oder nach einer Blockänderung wird die Struktur erneut geprüft.

## 4. Berechnungsmodell

### Pro Kernmodul

Empfohlene MVP-Basiswerte:

- Produktion: **80 FE/t**
- Wärme: **12 HU/t**
- Brenndauer eines Brennstabs: **2400 Ticks**
- Interner Energiepuffer: **100.000 FE je Kern**

### Steuerstäbe

Der Controller besitzt eine globale Einfahrstufe von 0 bis 100 Prozent.
Ein Kern kann höchstens von zwei direkt angrenzenden Steuerstab-Modulen profitieren.

```text
wirksameSteuerung = globaleEinfahrstufe × min(2, angrenzendeSteuerstäbe) / 2
Produktion = Basisproduktion × (1 - 0,75 × wirksameSteuerung)
Wärme      = Basiswärme      × (1 - 0,90 × wirksameSteuerung)
```

Damit bleibt bei vollständig eingefahrenen Stäben eine geringe Produktion übrig,
die Wärme sinkt jedoch stärker. Ein echter Not-Aus beendet die Reaktion vollständig.

### Kühlung

```text
Kühlleistung je Kern = 2 + 8 × verbundene angrenzende Kühlkanäle
Nettohitze            = Summe Kernwärme - Summe Kühlleistung
```

Kühlung wird nur für aktive Kernmodule berechnet. Unbenutzte Kühlrohre erzeugen
keine kostenlose globale Kühlleistung.

### Effizienzbonus

Mehrere Kerne dürfen nicht exponentiell skalieren. Ein kleiner Nachbarschaftsbonus
belohnt kompakte Entwürfe, bleibt aber begrenzt:

```text
Bonus je Kern = min(20 %, 5 % × direkt angrenzende aktive Kerne)
```

### Temperaturstufen

| Temperatur | Zustand | Wirkung |
|---:|---|---|
| 0–59 % | Stabil | Volle berechnete Leistung |
| 60–79 % | Heiß | Warnanzeige und mehr Strahlung |
| 80–94 % | Kritisch | Automatische Drosselung auf 50 % |
| 95–99 % | Notfall | Steuerstäbe vollständig einfahren |
| 100 % | Kernschmelze | Reaktor stoppt; Explosion nur bei deaktivierter Sicherheit |

Empfehlung: Eine Kernschmelze zerstört Innenmodule und kontaminiert die Umgebung,
anstatt immer sofort die gesamte Hülle zu sprengen. Das erzeugt interessantere
Reparaturfolgen und verhindert unverhältnismäßigen Weltverlust.

## 5. Ressourcenfluss

### Brennstoff und Abfall

- Jeder aktive Reaktorkern benötigt Brennstäbe aus dem zentralen Brennstoffpuffer.
- Ein Brennstab versorgt genau einen Kernzyklus.
- Nach Zyklusende entsteht garantiert ein verbrauchter Brennstab.
- Ist der Abfallpuffer voll, startet kein neuer Kernzyklus.
- Item-Ports werden im GUI als `INPUT`, `OUTPUT` oder `DISABLED` konfiguriert.

### Energie

- Energie wird zentral im Controller gespeichert.
- Kapazität: `100.000 FE × Anzahl Kernmodule`, mindestens 100.000 FE.
- Energie verlässt die Struktur ausschließlich über Energie-Ports.
- Transfer je Port: 4096 FE/t, durch mehrere Ports erweiterbar.
- Die gespeicherte Energiemenge darf niemals größer als die gemeldete Kapazität sein.

### Redstone

MVP-Modi:

- `IGNORED`: Redstone wird ignoriert.
- `HIGH_ENABLES`: Signal aktiviert den Reaktor.
- `HIGH_DISALBES`: Signal führt einen kontrollierten Stopp aus.
- `EMERGENCY_STOP`: Signal fährt Steuerstäbe sofort vollständig ein.

Schreibfehler vermeiden: Die tatsächliche Enum-Konstante sollte `HIGH_DISABLES` heißen.

Komparator-Ausgabe kann im GUI gewählt werden: Energie, Temperatur, Brennstoff oder Abfall.

## 6. Controller-GUI

Das GUI erhält vier Bereiche:

### Übersicht

- Strukturgröße und Anzahl der Module,
- aktuelle und maximale FE,
- Produktion und Ausgabe pro Tick,
- Temperatur, Nettohitze und Kühlleistung,
- Brennstoff- und Abfallfüllstand,
- großer Status: `INAKTIV`, `STABIL`, `HEISS`, `KRITISCH`, `FEHLER`.

### Steuerung

- Ein/Aus-Schalter,
- Steuerstab-Regler 0–100 Prozent,
- Redstone-Modus,
- automatische Abschalttemperatur.

### Diagnose

Konkrete Meldungen statt nur „ungültige Struktur“:

- `Gehäuselücke bei X/Y/Z`,
- `Kein Energie-Port vorhanden`,
- `Kernmodul ohne Kühlverbindung`,
- `Abfallausgang voll`,
- `Strukturgrenze überschritten`.

### Layout

Eine vereinfachte Schichtansicht zeigt Innenmodule farbig. Für den MVP genügt die
aktuelle Y-Schicht mit Pfeiltasten; eine echte 3D-Ansicht ist nicht erforderlich.

## 7. Technische Architektur

Empfohlene neue Typen:

```text
block/
  ModularReactorControllerBlock
  ReactorEnergyPortBlock
  ReactorItemPortBlock

block/entity/
  ModularReactorControllerBlockEntity
  ReactorPortBlockEntity

reactor/
  ReactorBounds
  ReactorLayout
  ReactorModuleStats
  ReactorValidator
  ReactorSimulation
  ReactorStatus
```

Aufgabentrennung:

- `ReactorValidator`: liest Weltblöcke und liefert Layout oder Diagnosefehler.
- `ReactorLayout`: unveränderliche, gespeicherte Modul- und Grenzdaten.
- `ReactorSimulation`: reine Berechnungen ohne Weltzugriff; gut testbar.
- Controller-BE: Inventar, Persistenz, Tick, Port-Verknüpfung und Synchronisierung.
- Ports: delegieren Zugriff an den Controller, speichern selbst keine Energie oder Items.

Persistiert werden mindestens Strukturgrenzen, Layout-Version, Energie, Temperatur,
Brennzeiten, Inventare, Steuerstabstellung, Redstone-Modus und Sicherheitsgrenze.
Geladene Werte müssen auf gültige Bereiche begrenzt werden.

## 8. Performance und Sicherheit

- Struktur nur bei Assemblierung, Blockänderung und alle 100 Ticks validieren.
- Tick arbeitet mit gecachten Modulwerten und ist damit O(Anzahl aktiver Kerne).
- Keine Rekursion für die Hüllensuche; begrenzte Queue/BFS verwenden.
- Ports speichern Controller-Position und prüfen Dimension, Entfernung und Blocktyp.
- GUI-Nutzung nur erlauben, wenn der Block noch existiert und der Spieler nah genug ist.
- Energieänderungen über `SimpleEnergyStorage` beziehungsweise Transaktionen ausführen.
- Server ist alleinige Wahrheitsquelle; Client erhält nur synchronisierte Anzeigewerte.

## 9. Migration und Kompatibilität

- Bestehende Einzelblock-Reaktoren bleiben unverändert.
- Der bisherige `multiblock_reactor_controller` kann entweder migriert oder als
  `legacy` markiert werden.
- Empfehlung: gleiche Block-ID behalten und beim Laden `layoutVersion = 0` erkennen.
- Alte 3×3×3-, 5×5×5- und 7×7×7-Strukturen können nach erneutem Assemblieren als
  neue modulare Struktur erkannt werden, sofern mindestens ein Kern eingebaut ist.
- Team Reborn Energy bleibt die einzige direkte Energie-Abhängigkeit.

## 10. Umsetzung in Etappen

### Phase A: Stabiler MVP

1. Validator für rechteckige Hülle und verständliche Fehler.
2. Kern-, Steuerstab- und Kühlrohr-Module erfassen.
3. Reine Simulationsklasse mit Unit-Tests.
4. Controller mit zentralem Brennstoff-, Abfall- und Energiespeicher.
5. Energie- und Item-Port.
6. Basis-GUI mit Übersicht, Ein/Aus und Steuerstab-Regler.

### Phase B: Automation und Darstellung

1. Redstone-Modi und konfigurierbare Komparator-Ausgabe.
2. Schichtansicht im GUI.
3. Partikel und Sounds abhängig vom Zustand.
4. Port-Seitenkonfiguration und bessere Tooltips.

### Phase C: Erweiterte Thermik

1. Flüssigkeitskühlung und Kühlmittel-Port.
2. Wärmetauscher und Moderator.
3. Kontamination und reparierbare Kernschmelze.
4. Konfigurierbare Balancewerte.

## 11. Abnahmekriterien für den MVP

- Eine gültige 3×3×3- und eine rechteckige 5×5×7-Struktur lassen sich assemblieren.
- Ein Loch in der Hülle deaktiviert den Reaktor spätestens nach 100 Ticks.
- Zwei unterschiedliche Innenlayouts erzeugen messbar andere Leistung und Hitze.
- Ein ausreichend gekühlter Reaktor kann einen vollständigen Brennzyklus überstehen.
- Jeder verbrauchte Brennstab erzeugt genau einen Abfallstab.
- Voller Abfallpuffer verhindert weitere Zündung ohne Brennstoffverlust.
- Energie wird nur durch Energie-Ports und niemals über normale Gehäuse abgegeben.
- NBT-Neuladen erhält Energie, Temperatur, Inventar und Steuerung korrekt.
- Entfernte Controller oder Ports können nicht über ein offenes GUI weiter benutzt werden.
- Die Berechnungslogik besitzt Tests für Kühlung, Steuerstäbe, Grenzwerte und Überhitzung.

## 12. Bewusste Nicht-Ziele des MVP

- keine beliebigen Freiform-Strukturen,
- keine Turbinen oder Dampfsimulation,
- keine echten Flüssigkeiten im ersten Schritt,
- keine netzwerkweite Fernsteuerung mehrerer Reaktoren,
- keine vollständige 3D-Darstellung im GUI.

Diese Begrenzung hält die erste Version umsetzbar und schafft trotzdem deutlich
mehr Bautiefe als das bisherige größenbasierte Multiblock-System.
