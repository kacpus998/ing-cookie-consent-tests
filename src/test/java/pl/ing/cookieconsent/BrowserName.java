package pl.ing.cookieconsent;

enum BrowserName {
    CHROMIUM("Chromium"),
    FIREFOX("Firefox"),
    WEBKIT("WebKit");

    private final String displayName;

    BrowserName(String displayName) {
        this.displayName = displayName;
    }

    String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

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
