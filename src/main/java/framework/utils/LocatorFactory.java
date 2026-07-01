package framework.utils;

import org.openqa.selenium.By;

public class LocatorFactory {

    private LocatorFactory() {
    }

    public static By getBy(ElementLocator elementLocator) {
        String byType = elementLocator.getBy();
        String locatorValue = elementLocator.getLocator();

        return switch (byType.toLowerCase()) {
            case "id" -> By.id(locatorValue);
            case "name" -> By.name(locatorValue);
            case "classname" -> By.className(locatorValue);
            case "tagname" -> By.tagName(locatorValue);
            case "linktext" -> By.linkText(locatorValue);
            case "partiallinktext" -> By.partialLinkText(locatorValue);
            case "css", "cssselector" -> By.cssSelector(locatorValue);
            case "xpath" -> By.xpath(locatorValue);
            default -> throw new IllegalArgumentException("Unsupported locator type: " + byType);
        };
    }
}