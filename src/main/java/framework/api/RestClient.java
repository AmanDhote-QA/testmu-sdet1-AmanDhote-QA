package framework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight REST API Client using Java 11+ HttpClient (no external dependencies).
 *
 * <p>Supports GET, POST, PUT, DELETE operations with configurable auth tokens,
 * headers, and request bodies. Returns {@link ApiResponse} objects with
 * status code, body, headers, and response time.</p>
 *
 * <p><b>Design Decision:</b> We use {@code java.net.http.HttpClient} instead of
 * RestAssured or Apache HttpClient to keep the dependency footprint minimal.
 * The JDK's built-in client is sufficient for REST API testing and reduces
 * framework bloat.</p>
 */
public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final HttpClient httpClient;
    private String authToken;
    private final Map<String, String> defaultHeaders = new HashMap<>();

    public RestClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.defaultHeaders.put("Content-Type", "application/json");
        this.defaultHeaders.put("Accept", "application/json");
        log.info("RestClient initialized — baseUrl={}", baseUrl);
    }

    /**
     * Sets the Bearer token for authenticated requests.
     */
    public RestClient withAuthToken(String token) {
        this.authToken = token;
        return this;
    }

    /**
     * Adds a custom header to all subsequent requests.
     */
    public RestClient withHeader(String name, String value) {
        this.defaultHeaders.put(name, value);
        return this;
    }

    // ── HTTP Methods ──

    public ApiResponse get(String endpoint) {
        return execute("GET", endpoint, null);
    }

    public ApiResponse post(String endpoint, Object body) {
        return execute("POST", endpoint, body);
    }

    public ApiResponse put(String endpoint, Object body) {
        return execute("PUT", endpoint, body);
    }

    public ApiResponse delete(String endpoint) {
        return execute("DELETE", endpoint, null);
    }

    // ── Core Execution ──

    private ApiResponse execute(String method, String endpoint, Object body) {
        String url = baseUrl + endpoint;
        log.info("{} {}", method, url);

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30));

            // Apply headers
            defaultHeaders.forEach(requestBuilder::header);
            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }

            // Apply method and body
            if (body != null) {
                String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else if ("POST".equals(method) || "PUT".equals(method)) {
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString("{}"));
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());
            long responseTime = System.currentTimeMillis() - startTime;

            // Extract response headers
            Map<String, String> responseHeaders = new HashMap<>();
            response.headers().map().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    responseHeaders.put(key, values.get(0));
                }
            });

            ApiResponse apiResponse = new ApiResponse(
                    response.statusCode(),
                    response.body(),
                    responseHeaders,
                    responseTime
            );

            log.info("Response: {} ({} ms) — {} chars",
                    apiResponse.getStatusCode(), responseTime,
                    apiResponse.getBody() != null ? apiResponse.getBody().length() : 0);

            return apiResponse;

        } catch (Exception e) {
            log.error("Request failed: {} {} — {}", method, url, e.getMessage());
            throw new RuntimeException("API request failed: " + method + " " + url, e);
        }
    }
}
