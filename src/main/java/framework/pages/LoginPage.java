package framework.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login Page Object — encapsulates all interactions with the login page.
 *
 * <p>Targets: <a href="https://the-internet.herokuapp.com/login">The Internet - Login</a></p>
 *
 * <p>Uses JSON-driven locators from {@code LoginPageElements.json} via the
 * {@link BasePage} superclass. No hardcoded locators in Java code — all
 * locators are externalized for maintainability.</p>
 */
public class LoginPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);
    private static final String LOCATOR_FILE = "LoginPageElements.json";

    public LoginPage() {
        super(LOCATOR_FILE);
        log.debug("LoginPage initialized with locators from: {}", LOCATOR_FILE);
    }

    // ── Core Actions ──

    /**
     * Performs a complete login flow.
     *
     * @param username  Username to enter
     * @param password  Password to enter
     */
    public void login(String username, String password) {
        log.info("Logging in with username: {}", username);
        type("usernameField", username);
        type("passwordField", password);
        clickWhenReady("loginButton");
    }

    /**
     * Gets the error/success message displayed after a login attempt.
     */
    public String getFlashMessage() {
        return getText("flashMessage");
    }

    /**
     * Clicks the "Forgot Password?" link (if present).
     */
    public void clickForgotPassword() {
        log.info("Clicking 'Forgot Password' link");
        click("forgotPasswordLink");
    }

    // ── Verification Methods ──

    /**
     * Checks if the login form is displayed.
     */
    public boolean isLoginFormDisplayed() {
        try {
            return isDisplayed("usernameField") && isDisplayed("passwordField");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the login button is visible.
     */
    public boolean isLoginButtonDisplayed() {
        try {
            return isDisplayed("loginButton");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a success message is displayed (indicates successful login).
     */
    public boolean isLoginSuccessful() {
        try {
            String message = getFlashMessage();
            return message != null && message.contains("You logged into a secure area");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if an error message is displayed (indicates failed login).
     */
    public boolean isErrorDisplayed() {
        try {
            String message = getFlashMessage();
            return message != null && (message.contains("invalid") || message.contains("Your username is invalid")
                    || message.contains("Your password is invalid"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Navigates to the login page.
     */
    public void navigateTo(String baseUrl) {
        String loginUrl = baseUrl + "/login";
        log.info("Navigating to login page: {}", loginUrl);
        open(loginUrl);
    }

    /**
     * Gets the current page URL.
     */
    public String getPageUrl() {
        return getCurrentUrl();
    }
}
