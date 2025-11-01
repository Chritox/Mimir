# Training Tracker – Spring Boot Application

A local web application for managing and documenting employee trainings (including intervals, deadlines, and Excel export). Runs as a classic JAR or a native Windows EXE (no JVM required)—automated builds via GitHub Actions.

## Features

- Manage employees, trainings, and participation records
- Set training intervals (e.g., yearly, every two years)
- Record when employees took which training
- Display when the next training is due per employee/training
- Export data to Excel (.xlsx) files
- Designed for local use only on one computer (not a shared server)
- Automated build (JAR & native EXE) via GitHub Actions

## Getting Started

### Prerequisites

- Java 17+ (for classic JAR execution)
- No installation required for the EXE (GraalVM native image)
- Maven 3.8+ (for local build/test)


## Building & Artifacts

### Local Build Variants

JAR:
mvn clean package

Native EXE (on Linux, needs GraalVM. For Windows, use GitHub Actions)


## Excel Export

In the web frontend (e.g., employees/trainings overview), use the “Export to Excel” button. The generated .xlsx file opens with Excel, LibreOffice Calc, etc.


## License

This project is licensed under the GNU General Public License v3.0 (GPL v3).
See the LICENSE file for more information.

