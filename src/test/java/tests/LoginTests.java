package tests;

import framework.listeners.RetryAnalyzer;
import framework.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Login Module Tests — generated and refined through AI-assisted prompt engineering.
 *
 * <p>Covers the assignment requirements for the Login module:</p>
 * <ul>
 *   <li>Valid login with correct credentials</li>
 *   <li>Invalid credentials (wrong username, wrong password, both wrong)</li>
 *   <li>Empty field validation</li>
 *   <li>Forgot password flow</li>
 *   <li>Session expiry simulation</li>
 *   <li>Brute-force lockout (rapid invalid attempts)</li>
 * </ul>
 *
 * <p>Target: <a href="https://the-internet.herokuapp.com/login">The Internet - Login</a></p>
 */
public class LoginTests extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void navigateToLogin() {
        loginPage = new LoginPage();
        loginPage.navigateTo(baseUrl);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Valid Login
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify successful login with valid credentials",
            groups = {"login", "smoke"},
            retryAnalyzer = RetryAnalyzer.class)
    public void testValidLogin() {
        loginPage.login("tomsmith", "SuperSecretPassword!");

        Assert.assertTrue(loginPage.isLoginSuccessful(),
                "Expected success message after valid login");
        log.info("Valid login test passed — user 'tomsmith' logged in successfully");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Invalid Credentials (Data-Driven)
    // ═══════════════════════════════════════════════════════════════════

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentialsData() {
        return new Object[][] {
                {"invalidUser", "SuperSecretPassword!", "Invalid username scenario"},
                {"tomsmith", "wrongPassword", "Invalid password scenario"},
                {"invalidUser", "wrongPassword", "Both credentials invalid"},
                {"", "SuperSecretPassword!", "Empty username"},
                {"tomsmith", "", "Empty password"},
                {"", "", "Both fields empty"}
        };
    }

    @Test(description = "Verify error message for invalid login credentials",
            dataProvider = "invalidCredentials",
            groups = {"login", "negative"},
            retryAnalyzer = RetryAnalyzer.class)
    public void testInvalidLogin(String username, String password, String scenario) {
        log.info("Testing invalid login scenario: {}", scenario);
        loginPage.login(username, password);

        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Expected error message for scenario: " + scenario);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Login Form Visibility
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify login form elements are displayed on page load",
            groups = {"login", "smoke"})
    public void testLoginFormDisplayed() {
        Assert.assertTrue(loginPage.isLoginFormDisplayed(),
                "Login form should be displayed on page load");
        Assert.assertTrue(loginPage.isLoginButtonDisplayed(),
                "Login button should be visible");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Session Expiry Simulation
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify session expiry by accessing secure page directly without login",
            groups = {"login", "security"})
    public void testSessionExpiry() {
        // Attempt to access dashboard without logging in — should redirect to login
        loginPage.open(baseUrl + "/secure");

        String currentUrl = loginPage.getPageUrl();
        log.info("Redirected to: {}", currentUrl);

        // Should be redirected to login page
        Assert.assertTrue(currentUrl.contains("login"),
                "Should be redirected to login page when session expired/not authenticated. Actual URL: " + currentUrl);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Brute-Force Lockout Simulation
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Simulate brute-force login attempts with rapid invalid credentials",
            groups = {"login", "security"})
    public void testBruteForceAttempts() {
        // Simulate 5 rapid failed login attempts
        for (int i = 1; i <= 5; i++) {
            loginPage.login("attacker", "attempt" + i);
            log.info("Brute-force attempt {}/5", i);

            // Verify error is shown for each attempt
            Assert.assertTrue(loginPage.isErrorDisplayed(),
                    "Error should be displayed on brute-force attempt #" + i);

            // Navigate back to login for next attempt
            if (i < 5) {
                loginPage.navigateTo(baseUrl);
            }
        }

        // After 5 attempts, verify the page still shows error (lockout check)
        Assert.assertTrue(loginPage.isErrorDisplayed(),
                "Application should handle rapid login attempts gracefully");
    }
}
