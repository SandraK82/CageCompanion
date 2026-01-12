🌐 [English](README.md) | **Deutsch** | [Français](README.fr.md)

---

# CAGE Companion

Eine einfache Android-App zur Verfolgung des Kanülenalters (CAGE) mit Nightscout-Integration.

## Funktionen

- **CAGE-Anzeige**: Zeigt das aktuelle Kanülenalter im Format Tage:Stunden:Minuten
- **Farbcodierung**: Grün (OK), Gelb (Warnung), Rot (Kritisch) basierend auf konfigurierbaren Schwellwerten
- **Nightscout-Integration**: Liest CAGE von Nightscout und lädt neue Site-Change-Behandlungen hoch
- **Homescreen-Widget**: Kompaktes Widget mit CAGE und Farbcodierung
- **Permanente Benachrichtigung**: Immer sichtbarer Status in der Benachrichtigungsleiste mit Farbe
- **Automatische Aktualisierung**: App, Widget und Benachrichtigung aktualisieren sich jede Minute
- **Mehrsprachig**: Deutsch, Englisch und Französisch

## Screenshots

| Hauptbildschirm | Benachrichtigung |
|-----------------|------------------|
| Große kreisförmige CAGE-Anzeige mit Farbe | Permanente Benachrichtigung mit CAGE-Status |

## Installation

1. APK von [Releases](../../releases) herunterladen und installieren
2. Einstellungen öffnen und Nightscout-URL sowie API-Secret konfigurieren
3. Warnschwellwerte einstellen (Standard: Gelb bei 2 Tagen, Rot bei 2,5 Tagen)
4. Die App beginnt automatisch mit der CAGE-Verfolgung

## Verwendung

### Hauptbildschirm
- Große kreisförmige Anzeige zeigt aktuelles CAGE mit Farbcodierung
- **Aktualisieren-Symbol**: Manuell aktuelles CAGE von Nightscout abrufen
- **Einstellungen-Symbol**: Einstellungen öffnen
- **Kanüle gewechselt-Button**: Neuen Site-Change in Nightscout aufzeichnen

### Widget
- "CAGE Widget" zum Homescreen hinzufügen
- Zeigt CAGE mit farbcodiertem Hintergrund
- Aktualisiert sich automatisch jede Minute
- Antippen öffnet die App

### Benachrichtigung
- Permanente Benachrichtigung zeigt aktuelles CAGE
- Farbcodiert basierend auf deinen Schwellwerten
- Aktualisiert sich automatisch jede Minute

## Konfiguration

### Nightscout-Einstellungen
- **URL**: Deine Nightscout-Instanz-URL (z.B. `https://dein-nightscout.herokuapp.com`)
- **API-Secret**: Dein Nightscout API-Secret oder Token für Lese-/Schreibzugriff

### Schwellwert-Einstellungen
- **Gelb (Warnung)**: Alter, bei dem die Anzeige gelb wird (Standard: 2 Tage)
- **Rot (Kritisch)**: Alter, bei dem die Anzeige rot wird (Standard: 2 Tage 12 Stunden)

## Voraussetzungen

- Android 8.0 (API 26) oder höher
- Nightscout-Instanz mit API-Zugriff

## Aus Quellcode bauen

```bash
git clone https://github.com/yourusername/CageCompanion.git
cd CageCompanion
./gradlew assembleDebug
```

## Technologie-Stack

- Kotlin
- Jetpack Compose
- Ktor HTTP-Client
- DataStore Preferences
- Glance Widgets
- Foreground Service

## Datenschutz

- Alle Daten werden lokal auf deinem Gerät gespeichert
- Kommuniziert nur mit deiner persönlichen Nightscout-Instanz
- Keine Analysen oder Tracking

## Lizenz

MIT-Lizenz - siehe [LICENSE](LICENSE)

## Haftungsausschluss

Diese App ist nicht mit der Nightscout Foundation oder einem Diabetes-Gerätehersteller verbunden. Es handelt sich um eine unabhängige Companion-App für das persönliche Diabetes-Management. Konsultiere immer deinen Arzt für medizinische Entscheidungen.

---

Mit Sorgfalt für die Diabetes-Community erstellt.
