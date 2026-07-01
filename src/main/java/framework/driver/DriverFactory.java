package framework.driver;

import framework.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverFactory {

    private DriverFactory() {
    }

    public static void initDriver() {
        BrowserType browserType = ConfigReader.getBrowser();
        WebDriver driver = createDriver(browserType);
        configureDriver(driver);
        DriverManager.setDriver(driver);
    }

    private static WebDriver createDriver(BrowserType browserType) {
        if (browserType == BrowserType.AUTO) {
            return createAutoDriver();
        }
        return switch (browserType) {
            case CHROME -> createChromeDriver();
            case FIREFOX -> createFirefoxDriver();
            case EDGE -> createEdgeDriver();
            default -> throw new IllegalArgumentException("Unsupported browser: " + browserType);
        };
    }

    private static WebDriver createAutoDriver() {
        try {
            return createChromeDriver();
        } catch (Exception e1) {
            System.err.println("Chrome failed to initialize, falling back to Edge...");
            try {
                return createEdgeDriver();
            } catch (Exception e2) {
                System.err.println("Edge failed to initialize, falling back to Firefox...");
                return createFirefoxDriver();
            }
        }
    }

    private static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        if (ConfigReader.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }

        if (ConfigReader.getBoolean("privateMode")) {
            options.addArguments("--incognito");
        }

        options.addArguments("--start-maximized");
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();

        if (ConfigReader.getBoolean("headless")) {
            options.addArguments("-headless");
        }

        if (ConfigReader.getBoolean("privateMode")) {
            options.addArguments("-private");
        }

        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver() {
        WebDriverManager.edgedriver().setup();

        EdgeOptions options = new EdgeOptions();

        if (ConfigReader.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }

        if (ConfigReader.getBoolean("privateMode")) {
            options.addArguments("-inprivate");
        }

        options.addArguments("--start-maximized");
        return new EdgeDriver(options);
    }

    private static void configureDriver(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("implicitWait")));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("pageLoadTimeout")));
        driver.manage().window().maximize();
    }
}