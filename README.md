# ING Cookie Consent Tests

Browser-based end-to-end tests for the ING cookie-consent experience.

## Requirements

- Java 21
- Playwright browser dependencies (installed by the Gradle test setup)

## Run tests

```bash
./gradlew test
```

The end-to-end suite runs against Chromium, Firefox, and WebKit.

## Run GitHub Actions manually

In GitHub, open **Actions → Controlled E2E → Run workflow**, select the `main` branch, and start the workflow. It can also be dispatched with:

```bash
gh workflow run controlled-e2e.yml --ref main
```
