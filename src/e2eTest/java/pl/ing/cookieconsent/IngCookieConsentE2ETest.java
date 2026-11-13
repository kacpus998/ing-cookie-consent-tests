package pl.ing.cookieconsent;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngCookieConsentE2ETest {
    @DisplayName("first visit exposes consent and customization")
    @ParameterizedTest(name = "{displayName} — {0}")
    @MethodSource("selectedBrowsers")
    void firstVisitHasNoDecisionAndAllowsCustomization(BrowserName browserName) {
        withFreshPage(browserName, (context, page, consent) -> {
            consent.open();
            assertTrue(consent.consentDialogIsVisible(), "A fresh context should show the consent dialog");
            assertTrue(consent.consentCookieValue().isEmpty(), "A fresh context should have no consent decision");

            consent.openSettings();
            assertTrue(consent.settingsDialogIsVisible(), "The first-visit dialog should open preferences");
            assertTrue(consent.technicalCookiesAreEnabled(), "Technical cookies should be enabled");
            assertTrue(consent.technicalCookiesAreDisabledForEditing(), "Technical cookies should not be editable");
            assertFalse(consent.analyticalCookiesAreEnabled(), "Analytical cookies should default to off");
            assertFalse(consent.marketingCookiesAreEnabled(), "Marketing cookies should default to off");
        });
    }

    @DisplayName("analytical choice is saved independently from marketing")
    @ParameterizedTest(name = "{displayName} — {0}")
    @MethodSource("selectedBrowsers")
    void savesAnalyticalConsentAcrossReloadWithoutMarketingConsent(BrowserName browserName) {
        withFreshPage(browserName, (context, page, consent) -> {
            consent.open();
            consent.openSettings();
            assertFalse(consent.analyticalCookiesAreEnabled());
            assertFalse(consent.marketingCookiesAreEnabled());
            consent.toggleAnalyticalCookies();
            assertTrue(consent.analyticalCookiesAreEnabled());
            assertFalse(consent.marketingCookiesAreEnabled(),
                    "Enabling analytical cookies must not enable marketing cookies");
            consent.saveSelected();

            assertFalse(consent.consentDialogIsVisible(), "Saving selected preferences should close the dialog");
            assertEquals("3", consent.consentCookieValue().orElseThrow(),
                    "The observed analytical-only choice is stored as cookiePolicyGDPR=3");
            page.reload();
            assertFalse(consent.consentDialogIsVisible(), "The saved decision should survive reload");
            consent.openSettings();
            assertTrue(consent.analyticalCookiesAreEnabled());
            assertFalse(consent.marketingCookiesAreEnabled(),
                    "The saved analytical decision must leave marketing cookies off");
        });
    }

    @DisplayName("reject all keeps optional cookies disabled after reload")
    @ParameterizedTest(name = "{displayName} — {0}")
    @MethodSource("selectedBrowsers")
    void rejectsOptionalCookiesAndPersistsDecision(BrowserName browserName) {
        withFreshPage(browserName, (context, page, consent) -> {
            consent.open();
            consent.rejectAll();
            assertFalse(consent.consentDialogIsVisible(), "Rejecting optional cookies should close the dialog");
            assertEquals("1", consent.consentCookieValue().orElseThrow(),
                    "Live observation of Odrzuć wszystkie stores cookiePolicyGDPR=1");

            page.reload();
            assertFalse(consent.consentDialogIsVisible(), "Rejected consent should persist after reload");
            consent.openSettings();
            assertFalse(consent.analyticalCookiesAreEnabled());
            assertFalse(consent.marketingCookiesAreEnabled());
        });
    }

    @DisplayName("accept all persists both optional categories")
    @ParameterizedTest(name = "{displayName} — {0}")
    @MethodSource("selectedBrowsers")
    void acceptsAllCookiesAndPersistsDecision(BrowserName browserName) {
        withFreshPage(browserName, (context, page, consent) -> {
            consent.open();
            consent.acceptAll();
            assertFalse(consent.consentDialogIsVisible(), "Accepting all should close the dialog");
            assertEquals("4", consent.consentCookieValue().orElseThrow(),
                    "Live observation of Zaakceptuj wszystkie stores cookiePolicyGDPR=4");
            consent.openSettings();
            assertTrue(consent.analyticalCookiesAreEnabled());
            assertTrue(consent.marketingCookiesAreEnabled());

            page.reload();
            assertFalse(consent.consentDialogIsVisible(), "Acceptance should persist after reload");
            assertEquals("4", consent.consentCookieValue().orElseThrow(),
                    "The all-categories decision should retain its observed value after reload");
        });
    }

    @DisplayName("saved preference can be changed through cookie settings")
    @ParameterizedTest(name = "{displayName} — {0}")
    @MethodSource("selectedBrowsers")
    void changesSavedConsentAndPersistsNewCategories(BrowserName browserName) {
        withFreshPage(browserName, (context, page, consent) -> {
            consent.open();
            consent.openSettings();
            consent.toggleAnalyticalCookies();
            consent.saveSelected();
            String initialValue = consent.consentCookieValue().orElseThrow();
            assertEquals("3", initialValue, "The initial analytical-only decision should be stored as value 3");

            consent.openSettings();
            assertTrue(consent.analyticalCookiesAreEnabled());
            assertFalse(consent.marketingCookiesAreEnabled());
            consent.toggleAnalyticalCookies();
            consent.toggleMarketingCookies();
            assertFalse(consent.analyticalCookiesAreEnabled());
            assertTrue(consent.marketingCookiesAreEnabled());
            consent.saveSelected();
            String changedValue = consent.consentCookieValue().orElseThrow();
            assertNotEquals(initialValue, changedValue,
                    "Changing from analytical-only to marketing-only must change the stored decision");
            assertEquals("2", changedValue,
                    "The live marketing-only choice is stored as cookiePolicyGDPR=2");

            page.reload();
            assertFalse(consent.consentDialogIsVisible(), "The changed decision should persist after reload");
            assertEquals(changedValue, consent.consentCookieValue().orElseThrow(),
                    "The changed stored decision should be stable after reload");
            consent.openSettings();
            assertFalse(consent.analyticalCookiesAreEnabled());
            assertTrue(consent.marketingCookiesAreEnabled(),
                    "The changed preference should enable marketing without analytical consent");
        });
    }

    @DisplayName("consent does not leak between browser contexts")
    @ParameterizedTest(name = "{displayName} — {0}")
    @MethodSource("selectedBrowsers")
    void isolatesConsentBetweenIndependentContexts(BrowserName browserName) {
        try (Playwright playwright = Playwright.create();
             Browser browser = browserType(playwright, browserName).launch(
                     new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext acceptedContext = browser.newContext();
             Page acceptedPage = acceptedContext.newPage()) {
            IngCookieConsentPage acceptedConsent = new IngCookieConsentPage(acceptedPage);
            acceptedConsent.open();
            acceptedConsent.acceptAll();
            assertEquals("4", acceptedConsent.consentCookieValue().orElseThrow());

            try (BrowserContext freshContext = browser.newContext(); Page freshPage = freshContext.newPage()) {
                IngCookieConsentPage freshConsent = new IngCookieConsentPage(freshPage);
                freshConsent.open();
                assertTrue(freshConsent.consentDialogIsVisible(),
                        "An independent context should show its own first-visit dialog");
                assertTrue(freshConsent.consentCookieValue().isEmpty(),
                        "Consent cookies must not be shared between independent contexts");
            }
        }
    }

    private static Stream<Arguments> selectedBrowsers() {
        BrowserName selected = BrowserName.parse(System.getProperty("browser"));
        return Stream.of(BrowserName.values())
                .filter(browser -> selected == BrowserName.ALL
                        ? browser != BrowserName.ALL
                        : browser == selected)
                .map(Arguments::of);
    }

    private static BrowserType browserType(Playwright playwright, BrowserName browserName) {
        return switch (browserName) {
            case CHROMIUM -> playwright.chromium();
            case FIREFOX -> playwright.firefox();
            case WEBKIT -> playwright.webkit();
            case ALL -> throw new IllegalArgumentException("'all' is a selection mode, not a browser.");
        };
    }

    private static void withFreshPage(BrowserName browserName, ConsentScenario scenario) {
        try (Playwright playwright = Playwright.create();
             Browser browser = browserType(playwright, browserName).launch(
                     new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext context = browser.newContext();
             Page page = context.newPage()) {
            scenario.run(context, page, new IngCookieConsentPage(page));
        }
    }

    @FunctionalInterface
    private interface ConsentScenario {
        void run(BrowserContext context, Page page, IngCookieConsentPage consent);
    }
}
