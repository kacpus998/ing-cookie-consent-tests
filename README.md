# ING Cookie Consent Tests

Small Java 21 test project for the cookie-consent interface on [ing.pl](https://www.ing.pl). It uses JUnit 5, Gradle, and Playwright for Java 1.51.0; no additional test framework is used.

## Test suites

- `src/test/java` contains unit tests. They test browser-name parsing only; `./gradlew test` does not launch a browser or contact ING.
- `src/e2eTest/java` contains Playwright end-to-end tests and the consent Page Object. Each scenario gets an independent browser context and checks the live site.

The E2E suite covers first-visit consent and access to settings, analytical-only consent, rejection of optional cookies, acceptance of all cookies, changing a saved choice, and isolation between browser contexts. The UI currently exposes separate analytical and marketing switches; tests verify that selecting one does not silently select the other. A separate “necessary only” scenario is omitted because saving the default optional-off settings duplicates the reject-all outcome.

Consent cookie values are asserted only for choices confirmed through the live interface: `cookiePolicyGDPR=1` after “Odrzuć wszystkie” (neither optional category), `=3` after selecting analytical cookies only, `=2` after selecting marketing cookies only, and `=4` after “Zaakceptuj wszystkie” (both optional categories). These are observed values for the corresponding choices; tests also verify each category through its UI switch rather than assuming a universal bit mapping.

## Requirements and setup

- JDK 21 (`java -version` should report 21).
- Internet access to resolve Gradle/dependencies and reach ING for E2E tests.
- Playwright browser system libraries on Linux, in addition to the downloaded browser binaries.
- No separately installed Gradle or Node.js; use the included Gradle Wrapper.

Install Chromium, Firefox, and WebKit before running E2E tests:

```sh
./gradlew playwrightInstall
```

On Linux, Playwright dependencies can also be installed where package-manager access is allowed:

```sh
./gradlew playwrightInstall --args='install-deps chromium firefox webkit'
```

## Running tests

```sh
./gradlew test       # unit tests only; no browser or ING access
./gradlew e2eTest    # E2E in Chromium, Firefox, and WebKit
./gradlew check      # unit and E2E suites
```

To select a single browser for E2E diagnosis, use `-Dbrowser=chromium`, `-Dbrowser=firefox`, or `-Dbrowser=webkit`. The default is all three browsers.

JUnit XML and HTML reports are written separately:

- Unit: `build/test-results/test/` and `build/reports/tests/test/index.html`
- E2E: `build/test-results/e2eTest/` and `build/reports/tests/e2eTest/index.html`

## GitHub Actions

**Controlled E2E** is manual-only (`workflow_dispatch`), is restricted to `main`, and runs `./gradlew check` on the existing self-hosted `ing-e2e` runner. It includes all three E2E browsers and uploads unit and E2E JUnit XML/HTML reports even when tests fail. It is not triggered by pushes or pull requests.

The E2E tests depend on live ING availability and the runner's installed Playwright browsers/system libraries. Network policy or site-side controls may affect results; do not bypass them.
