package pl.ing.cookieconsent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrowserNameTest {
    @Test
    void parsesSupportedBrowserNamesAndDefaultsToAllBrowsers() {
        assertEquals(BrowserName.ALL, BrowserName.parse(null));
        assertEquals(BrowserName.ALL, BrowserName.parse(""));
        assertEquals(BrowserName.CHROMIUM, BrowserName.parse("chromium"));
        assertEquals(BrowserName.FIREFOX, BrowserName.parse("firefox"));
        assertEquals(BrowserName.WEBKIT, BrowserName.parse("webkit"));
    }

    @Test
    void parsesAllBrowsersMode() {
        assertEquals(BrowserName.ALL, BrowserName.parse("all"));
    }

    @Test
    void rejectsUnsupportedBrowserNameWithReadableMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> BrowserName.parse("edge"));

        assertEquals("Unsupported browser 'edge'. Supported values: chromium, firefox, webkit, all.",
                exception.getMessage());
    }
}
