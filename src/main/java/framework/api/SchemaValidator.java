package framework.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * JSON Schema Validator — validates API response bodies against expected schemas.
 *
 * <p>Uses Jackson to compare the structure (field names and types) of an actual
 * JSON response against an expected schema definition. This is lighter than
 * JSON Schema Draft-07 validation but sufficient for catching structural regressions.</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *     boolean valid = SchemaValidator.validate(response.getBody(), "schemas/user_response.json");
 * </pre>
 */
public class SchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCHEMA_PATH = "src/test/resources/schemas/";

    private SchemaValidator() {
    }

    /**
     * Validates that a JSON response body contains all expected fields
     * defined in the schema file.
     *
     * @param responseBody   The actual JSON response body string
     * @param schemaFileName The schema file name (relative to schemas directory)
     * @return true if the response matches the expected schema structure
     */
    public static boolean validate(String responseBody, String schemaFileName) {
        try {
            JsonNode actual = objectMapper.readTree(responseBody);
            JsonNode expected = objectMapper.readTree(new File(SCHEMA_PATH + schemaFileName));

            return validateNode(actual, expected, "root");
        } catch (IOException e) {
            log.error("Schema validation failed — could not read schema file: {}", schemaFileName, e);
            return false;
        }
    }

    /**
     * Validates that a JSON response contains all fields defined in the expected structure.
     * Performs structural comparison — checks field presence and type matching.
     */
    public static boolean validateStructure(String responseBody, String expectedStructure) {
        try {
            JsonNode actual = objectMapper.readTree(responseBody);
            JsonNode expected = objectMapper.readTree(expectedStructure);

            return validateNode(actual, expected, "root");
        } catch (Exception e) {
            log.error("Schema structure validation failed: {}", e.getMessage());
            return false;
        }
    }

    private static boolean validateNode(JsonNode actual, JsonNode expected, String path) {
        if (expected.isObject()) {
            if (!actual.isObject()) {
                log.error("Schema mismatch at '{}': expected object, got {}", path, actual.getNodeType());
                return false;
            }

            Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                String fieldPath = path + "." + fieldName;

                if (!actual.has(fieldName)) {
                    log.error("Schema mismatch: missing field '{}' at path '{}'", fieldName, path);
                    return false;
                }

                if (!validateNode(actual.get(fieldName), entry.getValue(), fieldPath)) {
                    return false;
                }
            }
            return true;

        } else if (expected.isArray()) {
            if (!actual.isArray()) {
                log.error("Schema mismatch at '{}': expected array, got {}", path, actual.getNodeType());
                return false;
            }
            // For arrays, validate that at least the first element matches the schema element
            if (expected.size() > 0 && actual.size() > 0) {
                return validateNode(actual.get(0), expected.get(0), path + "[0]");
            }
            return true;

        } else {
            // Leaf node — just check type compatibility
            if (expected.isNumber() && !actual.isNumber()) {
                log.error("Schema mismatch at '{}': expected number, got {}", path, actual.getNodeType());
                return false;
            }
            if (expected.isTextual() && !actual.isTextual()) {
                log.error("Schema mismatch at '{}': expected string, got {}", path, actual.getNodeType());
                return false;
            }
            if (expected.isBoolean() && !actual.isBoolean()) {
                log.error("Schema mismatch at '{}': expected boolean, got {}", path, actual.getNodeType());
                return false;
            }
            return true;
        }
    }
}
