package pl.ing.cookieconsent;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrowserInfrastructureSmokeTest {
    @ParameterizedTest(name = "{0} launches and interacts with a local page")
    @MethodSource("selectedBrowser")
    void launchesBrowserAndInteractsWithLocalPage(BrowserName browserName) {
        try (Playwright playwright = Playwright.create();
             Browser browser = browserType(playwright, browserName).launch(
                     new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage();
            page.setContent("<main><h1 id='smoke-ready'>Playwright browser smoke test</h1></main>");

            assertEquals("Playwright browser smoke test", page.locator("#smoke-ready").textContent());
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

    private static BrowserType browserType(Playwright playwright, BrowserName browserName) {
        return switch (browserName) {
            case CHROMIUM -> playwright.chromium();
            case FIREFOX -> playwright.firefox();
            case WEBKIT -> playwright.webkit();
            case ALL -> throw new IllegalArgumentException("'all' is a selection mode, not a browser.");
        };
    }
}
