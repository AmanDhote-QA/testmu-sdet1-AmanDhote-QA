package framework.driver;

public enum BrowserType {
    CHROME,
    FIREFOX,
    EDGE,
    AUTO;

    public static BrowserType from(String browser) {
        if (browser == null || browser.isBlank()) {
            throw new IllegalArgumentException("Browser value cannot be null or blank");
        }

        try {
            return BrowserType.valueOf(browser.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unsupported browser: " + browser + ". Supported browsers are: CHROME, FIREFOX, EDGE, AUTO"
            );
        }
    }
}
