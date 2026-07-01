package framework.utils;

import framework.config.ConfigReader;
import framework.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {

    private WaitUtils() {
    }

    private static WebDriverWait getWait() {
        WebDriver driver = DriverManager.getDriver();
        return new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicitWait")));
    }

    public static WebElement waitForVisibility(By by) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitForClickable(By by) {
        return getWait().until(ExpectedConditions.elementToBeClickable(by));
    }

    public static WebElement waitForPresence(By by) {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static boolean waitForTitleContains(String titleText) {
        return getWait().until(ExpectedConditions.titleContains(titleText));
    }

    public static boolean waitForUrlContains(String urlText) {
        return getWait().until(ExpectedConditions.urlContains(urlText));
    }

    public static boolean waitForInvisibility(By by) {
        return getWait().until(ExpectedConditions.invisibilityOfElementLocated(by));
    }
}