package pl.ing.cookieconsent;

enum BrowserName {
    CHROMIUM,
    FIREFOX,
    WEBKIT;

    static BrowserName parse(String value) {
        if (value == null || value.isBlank()) {
            return CHROMIUM;
        }
        return switch (value) {
            case "chromium" -> CHROMIUM;
            case "firefox" -> FIREFOX;
            case "webkit" -> WEBKIT;
            default -> throw new IllegalArgumentException("Unsupported browser '" + value
                    + "'. Supported values: chromium, firefox, webkit.");
        };
    }
}
