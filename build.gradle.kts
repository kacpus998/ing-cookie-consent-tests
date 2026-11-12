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
