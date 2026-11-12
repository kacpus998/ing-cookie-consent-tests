plugins {
    java
}

group = "pl.ing.cookieconsent"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.microsoft.playwright:playwright:1.51.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("playwrightInstall") {
    group = "playwright"
    description = "Installs the Chromium, Firefox, and WebKit browsers used by Playwright."
    classpath = configurations.testRuntimeClasspath.get()
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "chromium", "firefox", "webkit")
}
