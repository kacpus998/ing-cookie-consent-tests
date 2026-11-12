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
    systemProperty("browser", System.getProperty("browser", "chromium"))
}

val e2eTestClass = "pl.ing.cookieconsent.IngCookieConsentE2ETest"
val testSourceSet = sourceSets.test.get()

fun registerBrowserTest(browserName: String) = tasks.register<Test>("${browserName}E2ETest") {
    description = "Runs the ING E2E test in $browserName."
    group = "verification"
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform()
    systemProperty("browser", browserName)
    filter {
        includeTestsMatching(e2eTestClass)
        isFailOnNoMatchingTests = true
    }
    reports {
        junitXml.outputLocation = layout.buildDirectory.dir("test-results/cross-browser/$browserName")
        html.outputLocation = layout.buildDirectory.dir("reports/tests/cross-browser/$browserName")
    }
}

val chromiumE2ETest = registerBrowserTest("chromium")
val firefoxE2ETest = registerBrowserTest("firefox")
val webkitE2ETest = registerBrowserTest("webkit")

firefoxE2ETest.configure { mustRunAfter(chromiumE2ETest) }
webkitE2ETest.configure { mustRunAfter(firefoxE2ETest) }

tasks.register("crossBrowserTest") {
    description = "Runs the ING E2E test in Chromium, Firefox, and WebKit."
    group = "verification"
    dependsOn(chromiumE2ETest, firefoxE2ETest, webkitE2ETest)

    doLast {
        listOf("chromium", "firefox", "webkit").forEach { browserName ->
            val browserDisplayName = when (browserName) {
                "chromium" -> "Chromium"
                "firefox" -> "Firefox"
                "webkit" -> "WebKit"
                else -> error("Unexpected browser: $browserName")
            }
            val report = layout.buildDirectory.file(
                "test-results/cross-browser/$browserName/TEST-$e2eTestClass.xml"
            ).get().asFile
            check(report.isFile) { "Missing JUnit XML report for $browserName: $report" }

            val document = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(report)
            val testCases = document.getElementsByTagName("testcase")
            val e2eCases = (0 until testCases.length).map { testCases.item(it) }
                .filter { it.attributes.getNamedItem("classname")?.nodeValue == e2eTestClass }
            check(e2eCases.size == 1) {
                "Expected exactly one executed $e2eTestClass test for $browserName; found ${e2eCases.size}."
            }
            val testCase = e2eCases.single()
            check(testCase.attributes.getNamedItem("name")?.nodeValue ==
                    "$browserDisplayName — savesAnalyticalCookieConsentAcrossReload" &&
                    testCase.childNodes.let { children ->
                        (0 until children.length).none { index ->
                            children.item(index).nodeName == "skipped"
                        }
                    }) {
                "The ING E2E test did not complete successfully for $browserName."
            }
        }
    }
}
