# Fehlerbericht: Gradle-Build im OneDrive-Arbeitsordner

Datum der Beobachtung: 2026-06-21  
Status: reproduziert; dauerhafter OneDrive-Workaround implementiert und bestätigt  
Projekt: AKW Mod, Minecraft 1.21.10

> Historischer Bericht aus der Fabric-Zeit. Das Projekt nutzt inzwischen NeoForge (ModDev statt
> Fabric Loom); die OneDrive-Ursachen (Files-On-Demand-Platzhalter, Gradle-FileHasher) und der
> Workaround gelten unverändert — Erwähnungen von „Fabric Loom" sind sinngemäß „NeoForge ModDev".

## 1. Kurzfassung

Gradle konnte im eigentlichen Projektordner unter OneDrive nicht bis zur
Projektkonfiguration oder Java-Kompilierung gelangen. Nach ungefähr zwei Minuten brach
es beim Erstellen des internen Gradle-Dienstes `FileHasher` mit einem Windows-
Cloud-Datei-Timeout ab.

Der gleiche Quellstand ließ sich nach einer gezielten Kopie in einen lokalen Ordner
außerhalb von OneDrive erfolgreich kompilieren, per Datagen verarbeiten und vollständig
bauen. Das spricht stark für ein Problem zwischen Gradles Dateizugriffen und OneDrive
Files On-Demand beziehungsweise Cloud-Platzhaltern, nicht für einen Fehler im Java-Code.

Zusätzlich existiert ein separates, eindeutig identifiziertes Konfigurationsproblem:
In `gradle.properties` ist ein macOS-JDK-Pfad fest eingetragen. Dieser Fehler tritt vor
dem OneDrive-Problem auf und muss für Tests unter Windows separat umgangen werden.

## 2. Betroffene Pfade

Projektordner:

```text
C:\Users\07785\OneDrive\programmieren\minecraft mod\akw mod
```

Erfolgreich verwendeter lokaler Build-Ordner:

```text
C:\Users\07785\AppData\Local\Temp\akw-codex-build-20260621001256
```

Der Projektordner, `src`, `src/main`, `src/main/java` und selbst Dateien wie
`gradle.properties` werden von Windows als `ReparsePoint` gemeldet. `fsutil` liefert
für den Projektordner und `gradle.properties` den Microsoft-Reparse-Tag:

```text
0x9000601a
```

Das ist mit einem durch OneDrive verwalteten Files-On-Demand-Pfad vereinbar. Der
Reparse-Tag allein beweist allerdings noch keinen Fehler; solche Pfade können im
Normalfall korrekt funktionieren.

## 3. Systemumgebung

Zum Zeitpunkt der Reproduktion:

| Komponente | Wert |
|---|---|
| Betriebssystem | Microsoft Windows NT 10.0.26200.0 |
| PowerShell | 5.1.26100.8655 |
| Dateisystem | NTFS, Status `Healthy` |
| Freier Speicher auf C: | etwa 1,25 TB |
| OneDrive | 26.098.0524.0004 |
| OneDrive-Stammordner | `C:\Users\07785\OneDrive` |
| Git | 2.52.0.windows.1 |
| Installiertes Java | Eclipse Temurin OpenJDK 25.0.2 |
| Java-Ziel des Projekts | Java 21 (`options.release = 21`) |
| Gradle | 9.5.0 |
| Fabric Loom | 1.17.12 |

Laufende OneDrive-Komponenten:

```text
C:\Program Files\Microsoft OneDrive\OneDrive.exe
C:\Program Files\Microsoft OneDrive\26.098.0524.0004\OneDrive.Sync.Service.exe
```

## 4. Separates Java-Konfigurationsproblem

In `gradle.properties` steht:

```properties
org.gradle.java.home=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

Das ist ein macOS-Pfad und unter Windows ungültig. Ein normaler Aufruf von
`gradlew.bat` bricht deshalb zunächst mit folgender Meldung ab:

```text
FAILURE: Build failed with an exception.

* What went wrong:
Value '/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home'
given for org.gradle.java.home Gradle property is invalid
(Java home supplied is invalid)
```

Für die weiteren Tests wurde diese Eigenschaft nur auf der Kommandozeile überschrieben:

```powershell
.\gradlew.bat `
  '-Dorg.gradle.java.home=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot' `
  compileJava --console=plain
```

Die Projektdatei wurde dafür nicht geändert. Nach dieser Korrektur wurde das eigentliche
OneDrive-Problem sichtbar.

## 5. Exakte Gradle-Fehlermeldung

Erster Build im OneDrive-Projektordner nach Überschreiben des JDK-Pfads:

- Gradle-Distribution 9.5.0 wurde erfolgreich geladen.
- Gradle-Daemon startete.
- Nach **2 Minuten 6 Sekunden** brach der Build ab.

Fehlermeldung:

```text
FAILURE: Build failed with an exception.

* What went wrong:
Gradle could not start your build.
> Could not create service of type BuildLifecycleController using
  BuildScopeServices.createBuildLifecycleController().
   > Could not create service of type BuildModelController using
     VintageBuildControllerProvider.createBuildModelController().
      > Could not create service of type FileHasher using
        BuildSessionServices.createFileHasher().
         > java.io.IOException: Der Cloudvorgang wurde nicht vor Ablauf der
           Zeitüberschreitungsperiode abgeschlossen

BUILD FAILED in 2m 6s
```

Wichtig: Es wurde noch kein `compileJava`-Fehler gemeldet. Gradle scheiterte beim
Initialisieren seines Datei-Hashers.

## 6. Zweiter Reproduktionsversuch

Um Gradles Dateisystembeobachtung und einen bestehenden Daemon auszuschließen, wurde
folgender Aufruf verwendet:

```powershell
.\gradlew.bat `
  '-Dorg.gradle.java.home=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot' `
  '-Dorg.gradle.vfs.watch=false' `
  --no-daemon compileJava --console=plain
```

Ergebnis:

- Gradle verwendete wie vorgesehen einen kurzlebigen Einzelprozess.
- Die VFS-Überwachung war deaktiviert.
- Nach **2 Minuten 19 Sekunden** trat dieselbe `FileHasher`-/Cloudvorgang-
  Fehlermeldung auf.

Damit sind ein langfristig laufender Gradle-Daemon und Gradles VFS-Watcher als alleinige
Ursache unwahrscheinlich.

## 7. Beobachtungen beim Kopieren

### 7.1 Vollständige Kopie mit `robocopy`

Eine rekursive Kopie des Projekts in `%TEMP%` blieb beim Abrufen von Dateien aus dem
OneDrive-Baum hängen. Nach mehr als 30 Sekunden lief `robocopy.exe` weiter, ohne die
erwarteten Quelldateien vollständig im Ziel bereitzustellen. Der Prozess musste später
beendet werden.

### 7.2 Lokaler Git-Klon

Auch ein lokaler Klon aus dem OneDrive-Arbeitsordner:

```powershell
git clone --no-hardlinks . <lokaler-temp-ordner>
```

blieb unvollständig. Der Zielordner wurde angelegt, enthielt aber keine ausgecheckten
Java-Quellen. Mehrere dabei gestartete Git-Prozesse liefen weiter und wurden beendet.

Das beweist nicht, dass Git selbst fehlerhaft ist. Wahrscheinlicher ist, dass auch der
Zugriff auf Arbeitsbaum oder Git-Objekte einen noch nicht lokal verfügbaren OneDrive-
Inhalt berührte.

### 7.3 Gezieltes lokales Vorhalten

Für die Java-Quellen wurde ausgeführt:

```powershell
attrib +p -u "src\main\java\*" /s /d
```

Danach ließ sich `src/main/java` mit `robocopy` sofort und vollständig in den lokalen
Build-Ordner kopieren. Es wurden 37 Java-Dateien kopiert.

Für Ressourcen und generierte Daten wurde der gleiche Versuch ausgeführt:

```powershell
attrib +p -u "src\main\resources\*" /s /d
attrib +p -u "src\main\generated\*" /s /d
```

Die Ressourcenkopie erreichte 48 Dateien. Die rekursive Kopie blieb anschließend erneut
hängen, während `src/main/generated` verarbeitet wurde.

Hinweis zum aktuellen Zustand: Durch diese Diagnosebefehle sind die Java- und
Ressourcendateien derzeit überwiegend mit dem OneDrive-Attribut `Pinned` markiert
(„Immer auf diesem Gerät behalten“). Das ist eine Änderung der Verfügbarkeitsattribute,
nicht des Dateiinhalts.

## 8. Auffällige Cloud-Platzhalter

Sieben Dateien unter `src/main/generated/.cache` blieben trotz des Pin-Versuchs mit
Cloud-/Offline-Attributen versehen:

```text
src/main/generated/.cache/0a32f90272ab0304a1861b4ef5f6f1f0bd4759ae  1503 Bytes
src/main/generated/.cache/5fc9767e15a178f3d159888eb2cd78a3020f8bc1   136 Bytes
src/main/generated/.cache/6de9dc61e152b38858c5c1ec226cb691363351f2   136 Bytes
src/main/generated/.cache/8d4c3a7b629022eeafee9d03ffdb41cdb6b77a4a   832 Bytes
src/main/generated/.cache/97599bc6d9e619e45a200abb93480397a4cc3dfe  6142 Bytes
src/main/generated/.cache/cc96eba61aa65b87274f4e0ed6005611cae96fe6  3801 Bytes
src/main/generated/.cache/eb915e2f302138a60bd2065aaaf6e3189f209de9   253 Bytes
```

Gemeldeter numerischer Attributwert:

```text
4724256
```

Dieser Wert setzt sich auf diesem System aus folgenden Windows-Dateiattributen zusammen:

- `Archive` (`0x20`)
- `SparseFile` (`0x200`)
- `ReparsePoint` (`0x400`)
- `Offline` (`0x1000`)
- `Pinned` (`0x80000`)
- `RecallOnDataAccess` (`0x400000`)

Die Kombination ist auffällig: Die Dateien sind gleichzeitig zum lokalen Vorhalten
markiert (`Pinned`), werden aber weiterhin als offline und beim Datenzugriff abzurufen
gemeldet (`Offline`, `RecallOnDataAccess`).

Diese sieben Dateien sind derzeit die stärkste konkrete Spur, weil:

1. Gradle ausdrücklich beim Datei-Hashing scheitert.
2. Der Fehler ein Windows-Cloudvorgang-Timeout ist.
3. Die rekursive Kopie beim generierten Verzeichnis hängen blieb.
4. Die Java-Quellen nach erfolgreichem Pinning sofort kopierbar waren.

Es ist dennoch noch nicht bewiesen, dass genau eine dieser sieben Dateien den Gradle-
Abbruch verursacht. Dafür fehlt ein Gradle-Stacktrace mit dem gerade geöffneten Pfad
oder eine Dateizugriffsaufzeichnung.

## 9. Beobachtung im lokalen Build-Ordner

Im lokalen Build-Ordner außerhalb von OneDrive liefen folgende Schritte erfolgreich:

```text
compileJava: BUILD SUCCESSFUL in 1m 24s
runDatagen:  BUILD SUCCESSFUL in 1m 3s
build:       BUILD SUCCESSFUL in 10s
```

Beim Datagen erschien einmal folgende Warnung für eine zuvor unvollständig kopierte
Cache-Datei:

```text
Failed to parse cache
...\src\main\generated\.cache\0a32f90272ab0304a1861b4ef5f6f1f0bd4759ae,
discarding
java.lang.IllegalStateException: Missing cache file header
```

Minecraft Datagen verwarf diesen Cache und erzeugte anschließend alle Daten erfolgreich
neu. Das deutet darauf hin, dass mindestens diese temporär kopierte Cache-Datei leer,
abgeschnitten oder nur teilweise verfügbar war. Ob die Quelldatei in OneDrive ebenfalls
inhaltlich beschädigt ist, wurde nicht geprüft.

## 10. Arbeitshypothesen

### Hypothese A: Files-On-Demand-Abruf läuft in ein Timeout

**Wahrscheinlichkeit: hoch.** Gradle versucht beim Hashen eine Offline-/Recall-Datei zu
lesen. OneDrive liefert die Daten nicht rechtzeitig, und der Windows-Cloudfilter gibt
`ERROR_CLOUD_OPERATION_TIMEOUT` beziehungsweise die deutsche Entsprechung zurück.

### Hypothese B: Inkonsistenter Pinning-Zustand der Datagen-Caches

**Wahrscheinlichkeit: hoch bis mittel.** Die sieben `.cache`-Dateien sind gleichzeitig
`Pinned`, `Offline` und `RecallOnDataAccess`. Dieser Zustand könnte aus einer noch nicht
abgeschlossenen Synchronisierung, einem OneDrive-Clientfehler oder nicht verfügbaren
Cloud-Inhalten stammen.

### Hypothese C: Sehr großer oder duplizierter Arbeitsbaum verstärkt das Problem

**Wahrscheinlichkeit: mittel.** Im Projektordner existieren zusätzliche Arbeitskopien
und Worktree-Verzeichnisse wie `codex`, `codes-branch` und `.claude/worktrees`. Diese
erhöhen die Zahl der von Tools sichtbaren Dateien deutlich. Ob Gradle sie beim
`FileHasher`-Start tatsächlich scannt, ist nicht belegt. Für allgemeine Kopier- und Git-
Operationen erhöhen sie aber sicher die Zahl möglicher Cloud-Abrufe.

### Hypothese D: OneDrive-Synchronisierung oder Cloudfilter ist intern blockiert

**Wahrscheinlichkeit: mittel.** Zwei `OneDrive.exe`-Prozesse und ein
`OneDrive.Sync.Service.exe` liefen. Das ist nicht automatisch ungewöhnlich. Denkbar sind
jedoch ein blockierter Download, eine fehlerhafte Sync-Datenbank oder ein temporäres
Netzwerk-/Dienstproblem.

### Nicht als Hauptursache passend

- Zu wenig Speicherplatz: Auf C: waren etwa 1,25 TB frei.
- Defektes lokales Dateisystem: NTFS meldete `Healthy`.
- Java-Compilerfehler: Derselbe Quellstand kompilierte außerhalb von OneDrive.
- Gradle-VFS-Watcher allein: Fehler blieb mit `org.gradle.vfs.watch=false` bestehen.
- Gradle-Daemon allein: Fehler blieb mit `--no-daemon` bestehen.

## 11. Empfohlene weitere Diagnose

Die folgenden Schritte sollten einzeln durchgeführt und protokolliert werden:

1. Im Explorer `src/main/generated/.cache` auswählen und **Immer auf diesem Gerät
   behalten** aktivieren. Warten, bis kein Cloud-Symbol mehr angezeigt wird.
2. Danach die Attribute erneut prüfen:

   ```powershell
   Get-ChildItem 'src\main\generated\.cache' -File |
     Select-Object Name, Length, Attributes
   ```

3. Jede der sieben Dateien einzeln lesen oder hashen und die Dauer messen:

   ```powershell
   Get-FileHash 'src\main\generated\.cache\<dateiname>' -Algorithm SHA256
   ```

4. Gradle mit Stacktrace erneut ausführen und das Log **außerhalb von OneDrive**
   speichern:

   ```powershell
   .\gradlew.bat `
     '-Dorg.gradle.java.home=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot' `
     '-Dorg.gradle.vfs.watch=false' `
     --no-daemon compileJava --stacktrace --info `
     *> "$env:TEMP\akw-gradle-onedrive.log"
   ```

5. Mit Sysinternals Process Monitor nach Zugriffen von `java.exe`, `gradle` und
   `OneDrive.exe` filtern. Besonders auf `src/main/generated/.cache`,
   `IO REPARSE TAG` und Cloudfilter-Fehler achten.
6. OneDrive-Synchronisierung testweise pausieren und den Build erneut starten.
7. Das Repository vollständig nach `C:\dev\akw-mod` oder einen anderen nicht
   synchronisierten Ordner kopieren und dort vergleichen.
8. Prüfen, ob `src/main/generated/.cache`, `.gradle`, `build` und temporäre Worktrees
   überhaupt synchronisiert beziehungsweise versioniert werden müssen.
9. OneDrive-Clientprotokolle und Windows-Ereignisanzeige zum Zeitpunkt des Fehlers
   prüfen.

Vor dem Löschen von `.cache` sollte geprüft werden, ob der Ordner ausschließlich von
Minecraft Datagen erzeugt wird. Der lokale Datagen-Lauf konnte ihn neu erzeugen, was
für einen regenerierbaren Cache spricht; eine Löschung wurde im eigentlichen Projekt
aber nicht durchgeführt.

## 12. Sinnvolle Suchbegriffe

Deutsch:

```text
Gradle FileHasher Cloudvorgang Zeitüberschreitungsperiode OneDrive
java.io.IOException Cloudvorgang wurde nicht vor Ablauf der Zeitüberschreitungsperiode abgeschlossen
OneDrive Pinned Offline RecallOnDataAccess gleichzeitig
Gradle OneDrive ReparsePoint FileHasher Windows
```

Englisch:

```text
Gradle FileHasher OneDrive cloud operation timed out
ERROR_CLOUD_OPERATION_TIMEOUT Gradle Windows
OneDrive Files On-Demand Pinned Offline RecallOnDataAccess
Gradle project inside OneDrive reparse point timeout
OneDrive placeholder file hashing Java IOException
```

## 13. Dauerhafte Lösung bei Repository in OneDrive

Das Repository bleibt vollständig in OneDrive. Nur flüchtige Gradle-/Loom-Daten werden
lokal gehalten.

Einmalig beziehungsweise nach neuen Checkouts:

```powershell
.\tools\prepare-onedrive.ps1
```

Das Skript markiert von Git verwaltete und neue, nicht ignorierte Projektdateien als
lokal verfügbar. Bekannte Hilfsarbeitskopien werden ausgelassen.

Builds werden über den neuen Wrapper gestartet:

```powershell
.\gradlew-onedrive.bat build --console=plain
```

Der Wrapper setzt:

```text
Gradle-Projektcache: %LOCALAPPDATA%\AKWMod\gradle-project-cache
Buildausgabe:        %LOCALAPPDATA%\AKWMod\build
Datagen-Laufordner:  %LOCALAPPDATA%\AKWMod\run\datagen
VFS-Watcher:         deaktiviert
```

`src/main/generated` bleibt im Repository, weil seine JSON-Dateien versioniert werden.
Der ignorierte Unterordner `.cache` wird nach Datagen automatisch entfernt.

## 14. Bestätigung und präzisierte Ursache

Nach dem Pinning von `src/main/generated/.cache` scheiterte ein normaler OneDrive-Build
weiterhin nach **1 Minute 57 Sekunden** mit demselben `FileHasher`-Timeout. Eine erneute
Attributprüfung zeigte danach:

- `.gradle` enthielt 299 Dateien mit `Pinned`, `Offline` und `RecallOnDataAccess`.
- Weitere 30 große Loom-Dateien waren weiterhin als Cloud-Abrufdateien markiert.
- Betroffen waren unter anderem `fileHashes.bin`, `resourceHashesCache.bin`, Locks,
  Ausführungshistorie, remappte Mods und Minecraft-JARs.
- `build` enthielt 339 und `run` 72 weitere ausgelagerte Dateien.

Damit ist der projektlokale `.gradle`-/Loom-Cache die wesentliche Ursache des konkreten
`FileHasher`-Fehlers; die sieben Datagen-Caches waren nur ein zusätzlicher Risikofaktor.

Mit lokalem Projektcache und lokaler Buildausgabe lief derselbe Build direkt aus dem
OneDrive-Repository erfolgreich:

```text
BUILD SUCCESSFUL in 46s
7 actionable tasks: 7 executed
```

Fabric Loom gab weiterhin einen allgemeinen Hinweis aus, dass ein Projekt in OneDrive
langsamer sein oder Probleme verursachen kann. Diese Warnung ist erwartet; der Build
selbst war erfolgreich.
