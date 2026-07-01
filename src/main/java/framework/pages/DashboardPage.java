package framework.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dashboard Page Object — encapsulates dashboard widget interactions.
 *
 * <p>Targets: <a href="https://the-internet.herokuapp.com/secure">The Internet - Secure Area</a></p>
 *
 * <p>Simulates a dashboard with widget verification, content checks, and
 * navigation elements. Uses JSON-driven locators from {@code DashboardPageElements.json}.</p>
 */
public class DashboardPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(DashboardPage.class);
    private static final String LOCATOR_FILE = "DashboardPageElements.json";

    public DashboardPage() {
        super(LOCATOR_FILE);
        log.debug("DashboardPage initialized with locators from: {}", LOCATOR_FILE);
    }

    // ── Verification Methods ──

    /**
     * Checks if the secure area / dashboard heading is displayed.
     */
    public boolean isDashboardLoaded() {
        try {
            return isDisplayed("secureAreaHeading");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the dashboard heading text.
     */
    public String getDashboardHeading() {
        return getText("secureAreaHeading");
    }

    /**
     * Gets the welcome/flash message text.
     */
    public String getWelcomeMessage() {
        return getText("flashMessage");
    }

    /**
     * Checks if the logout button is visible (permission-based visibility check).
     */
    public boolean isLogoutButtonVisible() {
        try {
            return isDisplayed("logoutButton");
        } catch (Exception e) {
            return false;
        }
    }

    // ── Actions ──

    /**
     * Clicks the logout button.
     */
    public void logout() {
        log.info("Logging out from dashboard");
        clickWhenReady("logoutButton");
    }

    /**
     * Navigates directly to the secure area.
     */
    public void navigateTo(String baseUrl) {
        String dashboardUrl = baseUrl + "/secure";
        log.info("Navigating to dashboard: {}", dashboardUrl);
        open(dashboardUrl);
    }

    /**
     * Gets the current page URL — useful for verifying redirect behavior.
     */
    public String getPageUrl() {
        return getCurrentUrl();
    }
}
