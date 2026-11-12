package pl.ing.cookieconsent;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngCookieConsentE2ETest {
    @Test
    void savesAnalyticalCookieConsentAcrossReload() {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext context = browser.newContext()) {
            var page = context.newPage();
            var consentBanner = new IngCookieConsentPage(page);

            consentBanner.open();
            consentBanner.customize();
            consentBanner.enableAnalyticalCookies();
            assertTrue(consentBanner.analyticalCookiesAreEnabled(),
                    "Analytical cookies switch should be enabled before saving");
            consentBanner.acceptSelected();

            assertTrue(consentBanner.isClosed(), "Consent dialog should close after saving");
            assertEquals("3", consentBanner.consentCookieValue().orElseThrow(),
                    "Cookie value should represent analytical consent");

            page.reload();

            assertEquals("3", consentBanner.consentCookieValue().orElseThrow(),
                    "Analytical consent should persist after reload");
            assertTrue(consentBanner.isClosed(), "Saved consent should keep the banner closed");
        }
    }
}
