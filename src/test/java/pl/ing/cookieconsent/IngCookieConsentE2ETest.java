package pl.ing.cookieconsent;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngCookieConsentE2ETest {
    @DisplayName("savesAnalyticalCookieConsentAcrossReload")
    @ParameterizedTest(name = "{0} — {displayName}")
    @MethodSource("selectedBrowser")
    void savesAnalyticalCookieConsentAcrossReload(BrowserName browserName) {
        try (Playwright playwright = Playwright.create();
             Browser browser = browserType(playwright, browserName).launch(
                     new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext context = browser.newContext()) {
            var page = context.newPage();
            var consentBanner = new IngCookieConsentPage(page);
            List<String> pageErrors = new ArrayList<>();
            List<String> consoleErrors = new ArrayList<>();
            List<String> navigationResponses = new ArrayList<>();
            Response[] mainNavigationResponse = new Response[1];

            page.onPageError(pageErrors::add);
            page.onConsoleMessage(message -> {
                if ("error".equalsIgnoreCase(message.type())) {
                    consoleErrors.add(message.text());
                }
            });
            page.onResponse(response -> {
                if (response.request().isNavigationRequest()
                        && response.frame().equals(page.mainFrame())
                        && mainNavigationResponse[0] == null) {
                    mainNavigationResponse[0] = response;
                    navigationResponses.add(E2EDiagnostics.redirectChain(response));
                }
            });

            try {
                consentBanner.open();
                assertTrue(consentBanner.isOpen(), "Consent dialog should be open for a fresh context");
                assertTrue(consentBanner.consentCookieValue().isEmpty(),
                        "Consent cookie should not exist before accepting the selection");
                consentBanner.customize();
                assertFalse(consentBanner.analyticalCookiesAreEnabled(),
                        "Analytical cookies switch should start disabled");
                consentBanner.toggleAnalyticalCookies();
                assertTrue(consentBanner.analyticalCookiesAreEnabled(),
                        "Clicking the analytical cookies switch should enable it");
                consentBanner.acceptSelected();

                assertTrue(consentBanner.isClosed(), "Consent dialog should close after saving");
                assertEquals("3", consentBanner.consentCookieValue().orElseThrow(),
                        "Cookie value should represent analytical consent");

                page.reload();

                assertEquals("3", consentBanner.consentCookieValue().orElseThrow(),
                        "Analytical consent should persist after reload");
                assertTrue(consentBanner.isClosed(), "Saved consent should keep the banner closed");
            } catch (RuntimeException | AssertionError failure) {
                E2EDiagnostics.capture(page, browserName, mainNavigationResponse[0],
                        navigationResponses, pageErrors, consoleErrors);
                throw failure;
            }
        }
    }

    private static Stream<Arguments> selectedBrowser() {
        BrowserName selected = BrowserName.parse(System.getProperty("browser"));
        return Stream.of(BrowserName.values())
                .filter(browser -> selected == BrowserName.ALL
                        ? browser != BrowserName.ALL
                        : browser == selected)
                .map(Arguments::of);
    }

    private static com.microsoft.playwright.BrowserType browserType(Playwright playwright,
                                                                     BrowserName browserName) {
        return switch (browserName) {
            case CHROMIUM -> playwright.chromium();
            case FIREFOX -> playwright.firefox();
            case WEBKIT -> playwright.webkit();
            case ALL -> throw new IllegalArgumentException("'all' is a selection mode, not a browser.");
        };
    }
}
