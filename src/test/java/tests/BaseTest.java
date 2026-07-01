package tests;

import framework.config.ConfigReader;
import framework.driver.DriverFactory;
import framework.driver.DriverManager;
import framework.listeners.AITestListener;
import framework.listeners.RetryAnalyzer;
import framework.listeners.ScreenshotListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

/**
 * Base Test — foundation class for all UI test classes.
 *
 * <p>Manages the WebDriver lifecycle per test method and wires in the
 * AI-powered listeners for failure analysis and reporting.</p>
 *
 * <p><b>Listener Ordering:</b> ScreenshotListener runs first to capture
 * the screenshot, then AITestListener captures the full context (including
 * screenshot path) for AI analysis.</p>
 *
 * <p><b>Thread Safety:</b> Uses DriverFactory/DriverManager with ThreadLocal
 * so tests can run in parallel without driver conflicts.</p>
 */
@Listeners({ScreenshotListener.class, AITestListener.class})
public class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected String baseUrl;
    protected String apiBaseUrl;

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        log.info("════════════════════════════════════════════════════");
        log.info("  TestMu SDET-1 AI-Native Framework");
        log.info("  Environment: {}", ConfigReader.get("environment"));
        log.info("  Browser: {}", ConfigReader.get("browser"));
        log.info("  AI Enabled: {}", ConfigReader.getBoolean("ai.enabled"));
        log.info("════════════════════════════════════════════════════");
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser"})
    public void setUp(@Optional String browser) {
        baseUrl = ConfigReader.get("baseUrl");
        apiBaseUrl = ConfigReader.get("apiBaseUrl");

        // Override browser from TestNG parameter if provided
        if (browser != null && !browser.isEmpty()) {
            System.setProperty("browser", browser);
        }

        DriverFactory.initDriver();
        log.info("WebDriver initialized for thread: {}", Thread.currentThread().getName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.unload();
        log.info("WebDriver closed for thread: {}", Thread.currentThread().getName());
    }

    @AfterSuite(alwaysRun = true)
    public void suiteTearDown() {
        log.info("════════════════════════════════════════════════════");
        log.info("  Test Suite Execution Complete");
        log.info("  Check reports/llm-output/ for AI analysis reports");
        log.info("════════════════════════════════════════════════════");
    }
}
