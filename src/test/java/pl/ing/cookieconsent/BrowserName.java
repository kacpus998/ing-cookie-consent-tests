package pl.ing.cookieconsent;

enum BrowserName {
    CHROMIUM("Chromium"),
    FIREFOX("Firefox"),
    WEBKIT("WebKit"),
    ALL("All browsers");

    private final String displayName;

    BrowserName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    static BrowserName parse(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return switch (value) {
            case "chromium" -> CHROMIUM;
            case "firefox" -> FIREFOX;
            case "webkit" -> WEBKIT;
            case "all" -> ALL;
            default -> throw new IllegalArgumentException("Unsupported browser '" + value
                    + "'. Supported values: chromium, firefox, webkit, all.");
        };
    }
}
