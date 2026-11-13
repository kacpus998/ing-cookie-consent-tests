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

val e2eTestSourceSet = sourceSets.create("e2eTest") {
    java.srcDir("src/e2eTest/java")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += output + compileClasspath
}

configurations[e2eTestSourceSet.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[e2eTestSourceSet.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "Runs the ING browser-based end-to-end tests."
    testClassesDirs = e2eTestSourceSet.output.classesDirs
    classpath = e2eTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    systemProperty("browser", providers.systemProperty("browser").orElse("all").get())
}

tasks.named("check") {
    dependsOn("e2eTest")
}

tasks.register<JavaExec>("playwrightInstall") {
    group = "playwright"
    description = "Installs the Chromium, Firefox, and WebKit browsers used by Playwright."
    classpath = configurations.testRuntimeClasspath.get()
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "chromium", "firefox", "webkit")
}
