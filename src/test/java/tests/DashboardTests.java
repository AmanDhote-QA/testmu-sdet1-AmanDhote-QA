package tests;

import framework.listeners.RetryAnalyzer;
import framework.pages.DashboardPage;
import framework.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Dashboard Module Tests — verifies dashboard functionality after login.
 *
 * <p>Covers the assignment requirements for the Dashboard module:</p>
 * <ul>
 *   <li>Widget/content loading after login</li>
 *   <li>Content accuracy (heading text)</li>
 *   <li>Permission-based visibility (logout button only when logged in)</li>
 *   <li>Responsive layout verification (page title present)</li>
 *   <li>Unauthorized access redirect</li>
 * </ul>
 *
 * <p>Target: <a href="https://the-internet.herokuapp.com/secure">The Internet - Secure Area</a></p>
 */
public class DashboardTests extends BaseTest {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @BeforeMethod
    public void loginAndNavigate() {
        loginPage = new LoginPage();
        dashboardPage = new DashboardPage();

        // Login first to access dashboard
        loginPage.navigateTo(baseUrl);
        loginPage.login("tomsmith", "SuperSecretPassword!");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Widget/Content Loading
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify dashboard content loads after successful login",
            groups = {"dashboard", "smoke"},
            retryAnalyzer = RetryAnalyzer.class)
    public void testDashboardLoadsAfterLogin() {
        Assert.assertTrue(dashboardPage.isDashboardLoaded(),
                "Dashboard heading should be displayed after login");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Data Accuracy
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify dashboard displays correct heading text",
            groups = {"dashboard", "regression"})
    public void testDashboardHeadingAccuracy() {
        String heading = dashboardPage.getDashboardHeading();
        log.info("Dashboard heading: '{}'", heading);

        Assert.assertNotNull(heading, "Dashboard heading should not be null");
        Assert.assertTrue(heading.contains("Secure Area"),
                "Dashboard heading should contain 'Secure Area'. Actual: " + heading);
    }

    @Test(description = "Verify welcome message is displayed on dashboard",
            groups = {"dashboard", "regression"})
    public void testWelcomeMessageDisplayed() {
        String message = dashboardPage.getWelcomeMessage();
        log.info("Welcome message: '{}'", message);

        Assert.assertNotNull(message, "Welcome message should not be null");
        Assert.assertTrue(message.contains("You logged into a secure area"),
                "Welcome message should confirm successful login");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Permission-Based Visibility
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify logout button is visible when user is authenticated",
            groups = {"dashboard", "security"})
    public void testLogoutButtonVisibleWhenLoggedIn() {
        Assert.assertTrue(dashboardPage.isLogoutButtonVisible(),
                "Logout button should be visible for authenticated users");
    }

    @Test(description = "Verify unauthorized access redirects to login page",
            groups = {"dashboard", "security"})
    public void testUnauthorizedAccessRedirect() {
        // Logout first
        dashboardPage.logout();

        // Try to access dashboard directly
        dashboardPage.navigateTo(baseUrl);

        String currentUrl = dashboardPage.getPageUrl();
        log.info("After unauthorized access attempt, URL: {}", currentUrl);

        Assert.assertTrue(currentUrl.contains("login"),
                "Unauthorized access should redirect to login. Actual URL: " + currentUrl);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Logout Flow
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify successful logout from dashboard",
            groups = {"dashboard", "smoke"})
    public void testLogoutFromDashboard() {
        dashboardPage.logout();

        // After logout, login form should be displayed
        Assert.assertTrue(loginPage.isLoginFormDisplayed(),
                "Login form should be displayed after logout");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Page URL Verification
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify dashboard URL is correct after login",
            groups = {"dashboard", "regression"})
    public void testDashboardUrl() {
        String currentUrl = dashboardPage.getPageUrl();
        log.info("Dashboard URL: {}", currentUrl);

        Assert.assertTrue(currentUrl.contains("/secure"),
                "Dashboard URL should contain '/secure'. Actual: " + currentUrl);
    }
}
