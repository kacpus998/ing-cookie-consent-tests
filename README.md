# ING Cookie Consent E2E Tests

## Overview

This project verifies that analytical cookie consent on [www.ing.pl](https://www.ing.pl) can be changed and remains saved after a page reload. The JUnit 5 end-to-end tests run against Chromium, Firefox, and WebKit using Playwright for Java 1.51.0. The project uses Java 21 and Gradle Kotlin DSL; the Gradle Wrapper pins the Gradle version.

## Prerequisites

- JDK 21 (the JDK, including `javac`, is required; the Gradle toolchain targets Java 21).
- Internet access to download Gradle/dependencies and Playwright browser binaries, and to reach ING's public website during E2E tests.
- Playwright-compatible system libraries for the selected browser engines. On Linux, see [Linux system dependencies](#linux-system-dependencies).
- No separately installed Gradle or Node.js is required. Use the included Wrapper (`./gradlew` or `gradlew.bat`); Playwright Java and its CLI are resolved through the project dependency.

## Quick Start

```sh
git clone <repository-url>
cd ing-cookie-consent-tests
java -version
```

Confirm that Java reports version 21, then install the three Playwright browsers and run tests:

```sh
./gradlew playwrightInstall
./gradlew test
```

On Windows, use `gradlew.bat` instead. Browser binaries are installed explicitly; ordinary `test` runs do not download browsers.

## Operating Systems

### Linux

Run the Wrapper commands above. Playwright browser executables require operating-system shared libraries in addition to the downloaded browser binaries. Playwright Java 1.51.0 supports installing these dependencies with its CLI:

```sh
./gradlew playwrightInstall --args='install-deps chromium firefox webkit'
```

This command may require administrator/root privileges to install packages via the system package manager. If you do not have those privileges, ask your system administrator to install the dependencies, or use a Linux environment/image where Playwright's browser dependencies are already present. `playwrightInstall` installs browser binaries only; it does not install OS packages.

### Windows

In Command Prompt or PowerShell, run:

```bat
git clone <repository-url>
cd ing-cookie-consent-tests
java -version
gradlew.bat playwrightInstall
gradlew.bat test
```

### macOS

Run the Wrapper commands from Terminal:

```sh
./gradlew playwrightInstall
./gradlew test
```

## Running Tests

Run the complete suite (all three browsers):

```sh
./gradlew test
```

To run only the ING E2E test for one browser, set the supported `browser` system property (`chromium`, `firefox`, `webkit`, or `all`):

```sh
./gradlew test -Dbrowser=firefox
```

JUnit XML results are written to `build/test-results/test/`; the HTML report is at `build/reports/tests/test/index.html`. Open the HTML file in a browser to inspect test outcomes and details.

## GitHub Actions

The **Controlled E2E** workflow is started manually from GitHub Actions (`workflow_dispatch`) and runs the full JUnit suite on a controlled self-hosted runner. It uploads JUnit XML and HTML reports as a workflow artifact, including when tests fail. E2E tests depend on reaching ING and on the runner's installed browser binaries and system libraries; network policy, proxies, allowlists, or site-side automation protections can affect results. The workflow is intentionally not a substitute for a locally controlled network environment.

## Troubleshooting

- **JDK 21 is missing or the wrong version is active:** install/select a JDK 21 and verify with `java -version`. Ensure `JAVA_HOME` and `PATH` point to it.
- **Playwright reports a missing browser executable:** run `./gradlew playwrightInstall` (or `gradlew.bat playwrightInstall` on Windows), then retry `test`.
- **Linux reports missing shared libraries:** run the `install-deps` command above in an environment where package installation is permitted, or ask an administrator to install Playwright's browser dependencies. The browser installation task alone does not provide these libraries.
- **ING blocks automation or the consent dialog is unavailable:** the site may apply security controls or behave differently for your network. Check access in a regular browser and retry from an approved network; do not attempt to bypass the site's protections.
- **Need diagnostic details:** inspect Gradle console output, `build/test-results/test/` (XML), and `build/reports/tests/test/index.html` (HTML). On GitHub Actions, download the `controlled-e2e-test-reports-*` artifact from the workflow run.
