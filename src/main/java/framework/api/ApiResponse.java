package framework.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Encapsulates an HTTP API response with convenience methods.
 *
 * <p>Provides typed access to status code, headers, body, and response time.
 * Helper methods like {@link #asJson()} and {@link #getJsonField(String)} make
 * assertions clean and readable in test code.</p>
 */
public class ApiResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;
    private final long responseTimeMs;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ApiResponse(int statusCode, String body, Map<String, String> headers, long responseTimeMs) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
        this.responseTimeMs = responseTimeMs;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    /**
     * Parses the response body as a Jackson JsonNode tree.
     */
    public JsonNode asJson() {
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response body as JSON: " + body, e);
        }
    }

    /**
     * Gets a specific field value from the JSON response body.
     *
     * @param fieldName  The top-level field name
     * @return The field value as text, or null if not found
     */
    public String getJsonField(String fieldName) {
        JsonNode node = asJson();
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : null;
    }

    /**
     * Checks if the response status code indicates success (2xx).
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", responseTimeMs=" + responseTimeMs +
                ", bodyLength=" + (body != null ? body.length() : 0) +
                '}';
    }
}
