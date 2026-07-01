package framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonLocatorReader {

    private static final String LOCATOR_PATH = "src/main/resources/locators/";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonLocatorReader() {
    }

    public static Map<String, ElementLocator> getLocators(String fileName) {
        try {
            File file = new File(LOCATOR_PATH + fileName);
            JsonNode rootNode = objectMapper.readTree(file);

            String rootKey = rootNode.fieldNames().next();
            JsonNode pageNode = rootNode.get(rootKey);

            PageElementWrapper wrapper = objectMapper.treeToValue(pageNode, PageElementWrapper.class);

            Map<String, ElementLocator> locatorMap = new LinkedHashMap<>();

            if (wrapper.getElements() != null) {
                for (ElementLocator element : wrapper.getElements()) {
                    locatorMap.put(element.getElementName(), element);
                }
            }

            return locatorMap;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read locator JSON file: " + fileName, e);
        }
    }
}