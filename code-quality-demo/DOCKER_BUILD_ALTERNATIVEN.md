# Container-Images ohne eigenes Dockerfile

In VL3 hast du das Image mit einem einstufigen `Dockerfile` aus dem fertigen JAR gebaut.
Für Spring-Boot-Apps gibt es zwei verbreitete Alternativen, die das Image direkt aus Maven heraus bauen.

## Jib (in diesem Repo verwendet)

```bash
./mvnw compile jib:dockerBuild                    # Image in den lokalen Docker-Daemon
./mvnw compile jib:dockerBuild -Djib.from.platforms=linux/arm64   # auf Apple Silicon
./mvnw compile jib:build                          # direkt in die Registry pushen (ohne Docker)
```

- Kein Dockerfile und für `jib:build` kein Docker-Daemon nötig.
- Abhängigkeiten, Ressourcen und Klassen landen in getrennten Layern: Bei Codeänderungen wird nur der kleine Klassen-Layer neu hochgeladen.
- Reproduzierbare Images; Basis-Image und Ziel stehen in der `pom.xml` (`<from>`, `<to>`).
- In der CI: `.github/workflows/code-quality-demo-container.yml` (Login über `-Djib.to.auth.*` mit `GITHUB_TOKEN`).

## Spring Boot Buildpacks

```bash
./mvnw spring-boot:build-image
./mvnw spring-boot:build-image -Dspring-boot.build-image.publish=true
```

- Bereits im `spring-boot-maven-plugin` enthalten, keine zusätzliche Dependency.
- Braucht einen Docker-Daemon (oder Podman), der Build läuft in Containern der Paketo-Buildpacks.
- Konfigurationsbeispiel: `pom-buildpacks-example.xml`.

## Vergleich

| Verfahren | Dockerfile nötig | Docker-Daemon nötig | Stärke |
|---|---|---|---|
| Dockerfile (VL3) | ja | ja | volle Kontrolle, für jede Sprache |
| Jib | nein | nein (nur für `jib:dockerBuild`) | schnell, Layer-Caching |
| Buildpacks | nein | ja | Best Practices „von der Stange“ |

Egal wie das Image entsteht: Danach gehört ein Image-Scan (z. B. Trivy) in die Pipeline.
