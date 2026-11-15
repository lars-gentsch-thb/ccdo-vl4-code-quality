# ccdo-vl4-code-quality

[![code-quality-demo Build](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/code-quality-demo-build.yml/badge.svg)](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/code-quality-demo-build.yml)
[![SonarQube Cloud](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/code-quality-demo-sonar.yml/badge.svg)](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/code-quality-demo-sonar.yml)
[![Container](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/code-quality-demo-container.yml/badge.svg)](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/code-quality-demo-container.yml)
[![mutation-testing PIT](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/mutation-testing.yml/badge.svg)](https://github.com/lars-gentsch-thb/ccdo-vl4-code-quality/actions/workflows/mutation-testing.yml)

Demo-Repo zur Vorlesung 4 von **Cloud Computing: Entwicklung und Betrieb (CCDO)** an der TH Brandenburg (Thema Code Quality und Security in der Pipeline).
Du siehst hier, wie statische Analyse, Testabdeckung, Mutation Testing und Image-Scanning als Quality Gates in GitHub Actions laufen.
Alles mit Java 21 und Maven Wrapper, Spring Boot 4.1 (Spring Framework 7, JUnit 6) wie in [ccdo-vl3-docker](https://github.com/lars-gentsch-thb/ccdo-vl3-docker).

## Aufbau

```
code-quality-demo/      Spring-Boot-App mit JaCoCo, Checkstyle, PMD, SpotBugs, SonarQube Cloud, Jib
  pom.xml               vollständige Konfiguration aller Werkzeuge
  pom-jacoco.xml        Snippets für Übung 1 (je ein Werkzeug zum Übernehmen
  pom-checkstyle.xml      in dein eigenes Projekt, Abschnitt "Beginn" bis "Ende")
  pom-pmd.xml
  checkstyle.xml        Google Java Style (aus Checkstyle 14.4.0)
  pom-buildpacks-example.xml, DOCKER_BUILD_ALTERNATIVEN.md   Jib vs. Buildpacks vs. Dockerfile
mutation-testing/       Palindrom-Checker für Mutation Testing mit PIT (Übung 4)
.github/workflows/      ein Workflow pro Aufgabe, jeweils nur bei Änderungen im passenden Ordner
.github/dependabot.yml  Updates für beide Maven-Projekte und die Actions
```

## Lokal bauen

Du brauchst nur ein JDK 21; Maven lädt der Wrapper (`./mvnw`) beim ersten Aufruf selbst.

```bash
cd code-quality-demo
./mvnw verify            # Tests + JaCoCo-Check (>= 60 %), Checkstyle, PMD, SpotBugs
./mvnw spring-boot:run   # App auf http://localhost:8080
./mvnw site              # HTML-Reports unter target/site (PMD mit verlinktem Quelltext)
```

Wo du die Ergebnisse findest:

| Werkzeug | Ergebnis | Bricht den Build ab? |
|---|---|---|
| JaCoCo | `target/site/jacoco/index.html` | ja, unter 60 % abgedeckter Komplexität |
| Checkstyle | Konsole, `target/checkstyle-result.xml` | nein, nur Warnungen |
| PMD | Konsole, `target/pmd.xml` | ja, bei Regelverstößen |
| SpotBugs | `target/spotbugsXml.xml`, `./mvnw spotbugs:gui` | nein (`failOnError` in der `pom.xml` auf `true` setzen) |

Tests mit Endung `*Tests` laufen als Unit-Tests (Surefire), Tests mit Endung `*IT` als Integrationstests (Failsafe).
Eine andere Endung (z. B. `*Test`) wird stillschweigend nicht ausgeführt.

Hinweis zu Spring Boot 4: Der Web-Starter heißt jetzt `spring-boot-starter-webmvc`, für Web-Tests kommt der modulare Test-Starter `spring-boot-starter-webmvc-test` dazu.
`@SpringBootTest` liefert MockMvc nur noch zusammen mit `@AutoConfigureMockMvc` (Paket `org.springframework.boot.webmvc.test.autoconfigure`), siehe `HelloControllerTests`.

## Mutation Testing mit PIT

```bash
cd mutation-testing
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage
open target/pit-reports/index.html      # Linux: xdg-open, Windows: start
```

Details zur Übung stehen in [mutation-testing/README.md](mutation-testing/README.md).

## SonarQube Cloud (früher SonarCloud) in 3 Schritten

1. Auf [sonarcloud.io](https://sonarcloud.io) mit GitHub anmelden, Organisation anlegen bzw. importieren und das Repository als Projekt hinzufügen. Unter *Administration → Analysis Method* die **Automatic Analysis abschalten** (sonst bricht die CI-Analyse ab).
2. Unter *My Account → Security* ein Token erzeugen und im GitHub-Repo unter *Settings → Secrets and variables → Actions* als Secret `SONAR_TOKEN` speichern.
3. In `code-quality-demo/pom.xml` die Properties `sonar.organization` und `sonar.projectKey` an deine Werte anpassen (stehen im Projekt unter *Information*). Beim nächsten Push nach `main` läuft der Workflow `code-quality-demo-sonar.yml` (`./mvnw verify sonar:sonar`); ohne Secret überspringt er die Analyse mit einer Warnung.

Lokal geht es auch: `SONAR_TOKEN=... ./mvnw verify sonar:sonar`.

## Container-Image in GHCR und Trivy-Scan

Der Workflow `code-quality-demo-container.yml` baut nach den Tests mit [Jib](https://github.com/GoogleContainerTools/jib) ein Image (ohne Dockerfile, für amd64 und arm64), pusht es nach

```
ghcr.io/lars-gentsch-thb/ccdo-vl4-code-quality:latest   (zusätzlich mit dem Commit-SHA als Tag)
```

und scannt es danach mit [Trivy](https://trivy.dev):

- Alle Funde ab HIGH landen als SARIF im Tab *Security → Code scanning* des Repos.
- Quality Gate: Gibt es CRITICAL- oder HIGH-Schwachstellen, für die schon ein Fix existiert, wird der Workflow rot.

```bash
docker run -p 8080:8080 ghcr.io/lars-gentsch-thb/ccdo-vl4-code-quality:latest
trivy image ghcr.io/lars-gentsch-thb/ccdo-vl4-code-quality:latest    # lokal scannen
./mvnw compile jib:dockerBuild -Djib.from.platforms=linux/arm64     # Image lokal (Apple Silicon)
```

**Warum ist die Trivy-Action per Commit-SHA eingebunden?** Im März 2026 wurden bei `aquasecurity/trivy-action` fast alle Versions-Tags auf Schadcode umgebogen, der Secrets aus CI-Läufen abgegriffen hat ([GHSA-69fq-xp46-6x23](https://github.com/aquasecurity/trivy/security/advisories/GHSA-69fq-xp46-6x23), CVE-2026-33634).
Ein Tag wie `@v0.36.0` kann jederzeit auf einen anderen Commit zeigen, ein voller Commit-SHA nicht.
Deshalb sind Actions von Drittanbietern hier per SHA gepinnt (die Version steht im Kommentar daneben, Dependabot aktualisiert beides); GitHubs eigene `actions/*` nutzen den Major-Tag.

Damit andere das Image ohne Login ziehen können, muss das Package in GitHub auf *public* stehen (*Package settings → Change visibility*).
