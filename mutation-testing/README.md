# Mutation Testing mit PIT am Beispiel eines Palindroms

Übung 4 der VL4: Du schreibst Tests für `PalindromeChecker` und prüfst mit [PIT](https://pitest.org), wie gut deine Tests wirklich sind.
PIT verändert den Code an vielen kleinen Stellen (Mutanten) und schaut, ob deine Tests das merken.
Überlebt ein Mutant, fehlt dir ein Test.

- Code: `src/main/java/tdd/PalindromeChecker.java`
- Tests: `src/test/java/tdd/PalindromeCheckerTest.java` (die leeren Tests füllst du aus)

## PIT ausführen

```bash
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage
```

Den Report findest du unter `target/pit-reports/index.html`.
In GitHub Actions läuft PIT bei jedem Push nach `main` (Workflow `mutation-testing.yml`), der Report liegt dort als Artifact `pit-report`.

Optional: In der `pom.xml` kannst du `<mutationThreshold>` einkommentieren. Dann bricht der Build ab, wenn der Mutation Score darunter liegt.

## Definition

Für diese Aufgabe ist ein Palindrom definiert
als Zeichenkette, die aus druckbaren Zeichen
besteht, die sowohl von rechts nach links
als auch von links nach rechts gelesen werden kann
und das selbe Wort ergibt.

### Beispiele für gültige Palindrome

* "anna"
* "otto"
* "ehe"
* "Retsinakanister"
* ">!<"

### Beispiele für ungültige Palindrome

* "!!>"
* " anna"
