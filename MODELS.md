# Modelle für Mitarbeiter, Schulungen und deren Zuordnungen

Diese Dokumentation beschreibt die Datenmodelle für die Verwaltung von Mitarbeitern (Employees), Schulungen (Trainings) und deren Zuordnungen (Training Participations).

## Überblick der Modelle

### 1. Employee (Mitarbeiter)
**Pfad:** `src/main/java/de/chritox/mimir/model/Employee.java`

Repräsentiert einen Mitarbeiter im System.

**Felder:**
- `id` (Long) - Eindeutige ID (automatisch generiert)
- `firstName` (String) - Vorname (Pflichtfeld)
- `lastName` (String) - Nachname (Pflichtfeld)
- `employeeNumber` (String) - Personalnummer (eindeutig)
- `email` (String) - E-Mail-Adresse (eindeutig)
- `department` (String) - Abteilung
- `trainingParticipations` (Set) - Zuordnung zu absolvierten Schulungen

### 2. Training (Schulung)
**Pfad:** `src/main/java/de/chritox/mimir/model/Training.java`

Repräsentiert eine Schulung, die von Mitarbeitern absolviert werden kann.

**Felder:**
- `id` (Long) - Eindeutige ID (automatisch generiert)
- `name` (String) - Name der Schulung (Pflichtfeld)
- `description` (String) - Beschreibung der Schulung
- `intervalMonths` (Integer) - Intervall in Monaten (z.B. 12 für jährlich, 24 für alle zwei Jahre) (Pflichtfeld)
- `mandatory` (Boolean) - Gibt an, ob die Schulung verpflichtend ist (Standard: false)
- `trainingParticipations` (Set) - Zuordnung zu Mitarbeitern, die diese Schulung absolviert haben

### 3. TrainingParticipation (Schulungsteilnahme / Zuordnungstabelle)
**Pfad:** `src/main/java/de/chritox/mimir/model/TrainingParticipation.java`

Repräsentiert die Zuordnung zwischen einem Mitarbeiter und einer Schulung. Diese Junction-Tabelle speichert zusätzliche Informationen über die Teilnahme.

**Felder:**
- `id` (Long) - Eindeutige ID (automatisch generiert)
- `employee` (Employee) - Referenz zum Mitarbeiter (Pflichtfeld)
- `training` (Training) - Referenz zur Schulung (Pflichtfeld)
- `completionDate` (LocalDate) - Datum der Absolvierung (Pflichtfeld)
- `nextDueDate` (LocalDate) - Datum der nächsten fälligen Schulung (wird automatisch berechnet)
- `notes` (String) - Optionale Notizen zur Teilnahme

## Repository-Layer

Die Repositories befinden sich unter `src/main/java/de/chritox/mimir/repository/`:

### EmployeeRepository
- `findByEmployeeNumber(String)` - Suche nach Personalnummer
- `findByEmail(String)` - Suche nach E-Mail-Adresse

### TrainingRepository
- `findByMandatory(Boolean)` - Suche nach verpflichtenden/optionalen Schulungen

### TrainingParticipationRepository
- `findByEmployee(Employee)` - Alle Schulungen eines Mitarbeiters
- `findByTraining(Training)` - Alle Teilnehmer einer Schulung
- `findByNextDueDateBefore(LocalDate)` - Überfällige Schulungen
- `findByEmployeeAndTraining(Employee, Training)` - Spezifische Zuordnung

## Service-Layer

Die Services befinden sich unter `src/main/java/de/chritox/mimir/service/`:

### EmployeeService
Geschäftslogik für Mitarbeiter-Operationen (CRUD)

### TrainingService
Geschäftslogik für Schulungs-Operationen (CRUD)

### TrainingParticipationService
Geschäftslogik für Schulungsteilnahmen mit automatischer Berechnung des nächsten Fälligkeitsdatums

## REST API Endpunkte

Die Controller befinden sich unter `src/main/java/de/chritox/mimir/controller/`:

### Mitarbeiter-Endpunkte (`/api/employees`)
- `GET /api/employees` - Alle Mitarbeiter abrufen
- `GET /api/employees/{id}` - Einzelnen Mitarbeiter abrufen
- `POST /api/employees` - Neuen Mitarbeiter erstellen
- `PUT /api/employees/{id}` - Mitarbeiter aktualisieren
- `DELETE /api/employees/{id}` - Mitarbeiter löschen

### Schulungs-Endpunkte (`/api/trainings`)
- `GET /api/trainings` - Alle Schulungen abrufen
- `GET /api/trainings/{id}` - Einzelne Schulung abrufen
- `GET /api/trainings/mandatory` - Nur verpflichtende Schulungen
- `POST /api/trainings` - Neue Schulung erstellen
- `PUT /api/trainings/{id}` - Schulung aktualisieren
- `DELETE /api/trainings/{id}` - Schulung löschen

### Schulungsteilnahme-Endpunkte (`/api/participations`)
- `GET /api/participations` - Alle Teilnahmen abrufen
- `GET /api/participations/{id}` - Einzelne Teilnahme abrufen
- `GET /api/participations/employee/{employeeId}` - Alle Schulungen eines Mitarbeiters
- `GET /api/participations/training/{trainingId}` - Alle Teilnehmer einer Schulung
- `GET /api/participations/overdue` - Überfällige Schulungen
- `POST /api/participations` - Neue Teilnahme erstellen
- `PUT /api/participations/{id}` - Teilnahme aktualisieren
- `DELETE /api/participations/{id}` - Teilnahme löschen

## Datenbankschema

Die Tabellen werden automatisch von Hibernate/JPA erstellt:

### `employees` Tabelle
- Primärschlüssel: `id`
- Eindeutige Felder: `employee_number`, `email`

### `trainings` Tabelle
- Primärschlüssel: `id`

### `training_participations` Tabelle (Junction Table)
- Primärschlüssel: `id`
- Fremdschlüssel: `employee_id` → `employees.id`
- Fremdschlüssel: `training_id` → `trainings.id`

## H2 Datenbank-Konsole

Die H2 Datenbank-Konsole ist verfügbar unter: `http://localhost:8080/h2-console`

**Verbindungsdetails:**
- JDBC URL: `jdbc:h2:file:./data/mimir`
- Benutzername: `sa`
- Passwort: (leer)

## Beispiele

### Mitarbeiter erstellen
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Max",
    "lastName": "Mustermann",
    "employeeNumber": "E001",
    "email": "max@example.com",
    "department": "IT"
  }'
```

### Schulung erstellen
```bash
curl -X POST http://localhost:8080/api/trainings \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Brandschutzschulung",
    "description": "Jährliche Brandschutzunterweisung",
    "intervalMonths": 12,
    "mandatory": true
  }'
```

### Schulungsteilnahme erstellen
```bash
curl -X POST http://localhost:8080/api/participations \
  -H "Content-Type: application/json" \
  -d '{
    "employee": {"id": 1},
    "training": {"id": 1},
    "completionDate": "2025-01-15",
    "notes": "Erfolgreich absolviert"
  }'
```

## Tests

Integrationstests befinden sich unter: `src/test/java/de/chritox/mimir/model/EntityIntegrationTest.java`

Tests ausführen:
```bash
./mvnw test
```

## Technologie-Stack

- **Spring Boot 3.5.7** - Application Framework
- **Spring Data JPA** - Datenzugriff
- **Hibernate** - ORM
- **H2 Database** - Eingebettete Datenbank
- **Lombok** - Reduzierung von Boilerplate-Code
- **JUnit 5** - Testing Framework
