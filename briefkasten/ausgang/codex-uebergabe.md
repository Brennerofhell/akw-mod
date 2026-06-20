# Codex-Uebergabe an Claude Code

Datum: 2026-06-21

## Modularer Reaktor

Das vollstaendige Konzept liegt hier:

- `briefkasten/ausgang/modularer-reaktor-konzept.md`

Es beschreibt:

- frei skalierbare rechteckige Multiblock-Huellen,
- Reaktorkerne, Steuerstaebe und Kuehlkanaele als Innenmodule,
- Energie-, Item-, Redstone- und spaetere Kuehlmittel-Ports,
- Leistungs-, Hitze- und Kuehlungsberechnung,
- Controller-GUI und konkrete Diagnosemeldungen,
- technische Klassenaufteilung,
- Umsetzung in drei Phasen,
- Abnahmekriterien und Tests fuer den MVP.

## Empfohlener Start

1. `ReactorBounds`, `ReactorLayout` und `ReactorValidator` implementieren.
2. Die Berechnung in eine weltunabhaengige `ReactorSimulation` auslagern.
3. Zuerst Tests fuer Huelle, Kuehlung, Steuerstaebe und Ueberhitzung schreiben.
4. Danach Controller und Ports an die getestete Simulation anbinden.

## Wichtiger Hinweis

Der bestehende Multiblock-Code hat bekannte Risiken bei GUI-Slotzahl, Kuehlung und
Energiespeicher-Kapazitaet. Das neue System sollte diese Logik nicht ungeprueft
uebernehmen. Bestehende Einzelblock-Reaktoren sollen kompatibel bleiben.

Ein GitHub-Push wurde bewusst nicht erzwungen. Diese Briefkasten-Dateien sind die
saubere Uebergabe, ohne den laufenden Claude-Arbeitsbaum oder `main` zu veraendern.
