package framework.pages;

import framework.driver.DriverManager;
import framework.utils.ElementLocator;
import framework.utils.JsonLocatorReader;
import framework.utils.LocatorFactory;
import framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class BasePage {

    protected WebDriver driver;
    protected Map<String, ElementLocator> locatorMap;

    public BasePage(String locatorFileName) {
        this.driver = DriverManager.getDriver();
        this.locatorMap = JsonLocatorReader.getLocators(locatorFileName);
    }

    protected By getBy(String elementName) {
        ElementLocator elementLocator = locatorMap.get(elementName);

        if (elementLocator == null) {
            throw new RuntimeException("No locator found for element: " + elementName);
        }

        return LocatorFactory.getBy(elementLocator);
    }

    protected WebElement getElement(String elementName) {
        return WaitUtils.waitForVisibility(getBy(elementName));
    }

    protected void click(String elementName) {
        getElement(elementName).click();
    }

    protected void type(String elementName, String text) {
        WebElement element = getElement(elementName);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(String elementName) {
        return getElement(elementName).getText();
    }

    protected boolean isDisplayed(String elementName) {
        return getElement(elementName).isDisplayed();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void open(String url) {
        driver.get(url);
    }

    protected void clickWhenReady(String elementName) {
        WaitUtils.waitForClickable(getBy(elementName)).click();
    }

    protected boolean waitForElementToDisappear(String elementName) {
        return WaitUtils.waitForInvisibility(getBy(elementName));
    }
}