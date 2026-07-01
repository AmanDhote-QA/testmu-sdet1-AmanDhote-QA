package framework.listeners;

import framework.config.ConfigReader;
import framework.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Screenshot Listener — captures screenshots on test failure.
 *
 * <p>Runs BEFORE the AITestListener so that the screenshot path is available
 * in the test result attributes when the AI context is captured.</p>
 */
public class ScreenshotListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotListener.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void onTestFailure(ITestResult result) {
        if (!ConfigReader.getBoolean("screenshot.onFailure")) {
            return;
        }

        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver instanceof TakesScreenshot) {
                String screenshotDir = ConfigReader.get("screenshot.outputDir");
                Path dir = Paths.get(screenshotDir);
                Files.createDirectories(dir);

                String fileName = result.getTestClass().getRealClass().getSimpleName()
                        + "_" + result.getMethod().getMethodName()
                        + "_" + LocalDateTime.now().format(TIMESTAMP_FORMAT)
                        + ".png";

                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Path destination = dir.resolve(fileName);
                Files.copy(screenshot.toPath(), destination);

                // Store path in result attributes for AITestListener to consume
                result.setAttribute("screenshotPath", destination.toAbsolutePath().toString());
                log.info("📸 Screenshot saved: {}", destination);
            }
        } catch (Exception e) {
            log.warn("Failed to capture screenshot: {}", e.getMessage());
        }
    }
}
