
# Verwende ein Maven-Image mit Java 17.
# Dieses Image enthält:
# - Maven
# - Java Compiler
# - alles, was zum Bauen der Anwendung benötigt wird
FROM maven:3.9-eclipse-temurin-21 AS builder
# Wechsle in das Arbeitsverzeichnis /myapp.
# Falls der Ordner nicht existiert, wird er erstellt.
WORKDIR /myapp

# Kopiere die pom.xml aus dem Backend-Projekt
# in das aktuelle Arbeitsverzeichnis (/myapp).
COPY backend/pom.xml .

# Kopiere den gesamten src-Ordner
# in /myapp/src.
COPY backend/src ./src

# Führe Maven aus:
# clean   = löscht alte Build-Artefakte
# package = erzeugt die fertige JAR-Datei
# -DskipTests = Tests nicht ausführen
#
# Ergebnis:
# /myapp/target/ai-helpdesk-0.0.1-SNAPSHOT.jar
RUN mvn clean package -DskipTests


# ==========================================
# Stage 2: Runtime (Lieferbox)
# ==========================================

# Verwende ein schlankes Java-Runtime-Image.
#
# Enthält:
# - Java Runtime
#
# Enthält NICHT:
# - Maven
# - Source Code
# - Java Compiler
#
# Dadurch wird das finale Image kleiner.
FROM eclipse-temurin:21-jre

# Arbeitsverzeichnis für den laufenden Container.
WORKDIR /myapp

# Kopiere die fertige JAR-Datei aus Stage 1.
#
# Von:
# /myapp/target/*.jar
#
# Nach:
# /myapp/app.jar
#
# builder = Name der ersten Stage
COPY --from=builder /myapp/target/*.jar app.jar

# Dokumentation:
# Die Anwendung stellt Port 8089 bereit.
#
# Dieser Befehl öffnet keinen Port,
# sondern informiert Docker darüber,
# welcher Port genutzt wird.
EXPOSE 8089

# Starte die Spring-Boot-Anwendung.
#
# Docker erstellt dabei:
#
# Container
# └── PID 1
#      └── java -jar app.jar
#
# Solange dieser Prozess läuft,
# läuft auch der Container.
CMD ["java", "-jar", "app.jar"]