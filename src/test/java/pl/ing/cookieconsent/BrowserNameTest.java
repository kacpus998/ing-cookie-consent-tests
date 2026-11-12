package pl.ing.cookieconsent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrowserNameTest {
    @Test
    void parsesSupportedBrowserNamesAndDefaultsToChromium() {
        assertEquals(BrowserName.CHROMIUM, BrowserName.parse(null));
        assertEquals(BrowserName.CHROMIUM, BrowserName.parse("chromium"));
        assertEquals(BrowserName.FIREFOX, BrowserName.parse("firefox"));
        assertEquals(BrowserName.WEBKIT, BrowserName.parse("webkit"));
    }

    @Test
    void rejectsUnsupportedBrowserNameWithReadableMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> BrowserName.parse("edge"));

        assertEquals("Unsupported browser 'edge'. Supported values: chromium, firefox, webkit.",
                exception.getMessage());
    }
}
