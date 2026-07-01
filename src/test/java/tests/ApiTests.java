package tests;

import framework.api.ApiResponse;
import framework.api.RestClient;
import framework.api.SchemaValidator;
import framework.config.ConfigReader;
import framework.listeners.AITestListener;
import framework.listeners.RetryAnalyzer;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Module Tests — validates API endpoints without a browser.
 *
 * <p>Target: <a href="https://dummyjson.com">DummyJSON API</a></p>
 */
@Listeners({AITestListener.class})
public class ApiTests {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiTests.class);
    private RestClient client;

    @BeforeClass
    public void setUp() {
        String apiBaseUrl = ConfigReader.get("apiBaseUrl"); // https://dummyjson.com
        client = new RestClient(apiBaseUrl);
        log.info("API tests initialized — baseUrl={}", apiBaseUrl);
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD — READ (GET)
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "GET /users — retrieve list of users",
            groups = {"api", "smoke"},
            retryAnalyzer = RetryAnalyzer.class)
    public void testGetUsersList() {
        ApiResponse response = client.get("/users?limit=5");

        Assert.assertEquals(response.getStatusCode(), 200, "GET /users should return 200 OK");
        Assert.assertNotNull(response.asJson().get("users"), "Response should contain 'users' array");
        Assert.assertTrue(response.asJson().get("users").isArray(), "'users' should be an array");

        log.info("Retrieved {} users", response.asJson().get("users").size());
    }

    @Test(description = "GET /users/{id} — retrieve single user",
            groups = {"api", "smoke"})
    public void testGetSingleUser() {
        ApiResponse response = client.get("/users/2");

        Assert.assertEquals(response.getStatusCode(), 200, "GET /users/2 should return 200 OK");
        String email = response.asJson().get("email").asText();
        Assert.assertNotNull(email, "User should have an email");
        log.info("User 2 email: {}", email);
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD — CREATE (POST)
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "POST /users/add — create a new user",
            groups = {"api", "crud"})
    public void testCreateUser() {
        Map<String, String> user = new HashMap<>();
        user.put("firstName", "TestMu QA Engineer");
        user.put("lastName", "SDET-1");

        ApiResponse response = client.post("/users/add", user);

        Assert.assertTrue(response.isSuccessful(), "POST /users/add should return success");
        Assert.assertNotNull(response.getJsonField("id"), "Created user should have an 'id'");
        Assert.assertEquals(response.getJsonField("firstName"), "TestMu QA Engineer", "Created user first name should match");

        log.info("Created user with id: {}", response.getJsonField("id"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD — UPDATE (PUT)
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "PUT /users/{id} — update an existing user",
            groups = {"api", "crud"})
    public void testUpdateUser() {
        Map<String, String> updatedUser = new HashMap<>();
        updatedUser.put("lastName", "Updated Engineer");

        ApiResponse response = client.put("/users/2", updatedUser);

        Assert.assertEquals(response.getStatusCode(), 200, "PUT /users/2 should return 200 OK");
        Assert.assertEquals(response.getJsonField("lastName"), "Updated Engineer", "Updated last name should match");
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD — DELETE
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "DELETE /users/{id} — delete a user",
            groups = {"api", "crud"})
    public void testDeleteUser() {
        ApiResponse response = client.delete("/users/2");

        Assert.assertEquals(response.getStatusCode(), 200, "DELETE /users/2 should return 200 OK");
        Assert.assertEquals(response.getJsonField("isDeleted"), "true", "Response should indicate user was deleted");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Error Handling — 4xx
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "GET /users/{invalid_id} — verify 404 for non-existent user",
            groups = {"api", "negative"})
    public void testGetNonExistentUser() {
        ApiResponse response = client.get("/users/999999");

        Assert.assertEquals(response.getStatusCode(), 404, "GET non-existent user should return 404");
    }

    @Test(description = "POST /auth/login — verify 400 for invalid credentials",
            groups = {"api", "negative"})
    public void testApiLoginInvalidCredentials() {
        Map<String, String> body = new HashMap<>();
        body.put("username", "invaliduser");
        body.put("password", "wrongpass");

        ApiResponse response = client.post("/auth/login", body);

        Assert.assertEquals(response.getStatusCode(), 400, "API login with invalid credentials should return 400");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Auth Token Validation
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "POST /auth/login — verify successful token generation",
            groups = {"api", "auth"})
    public void testSuccessfulApiLogin() {
        Map<String, String> body = new HashMap<>();
        body.put("username", "emilys"); // Default valid user in dummyjson
        body.put("password", "emilyspass");

        ApiResponse response = client.post("/auth/login", body);

        Assert.assertEquals(response.getStatusCode(), 200, "Valid API login should return 200");
        Assert.assertNotNull(response.getJsonField("accessToken"), "Successful login should return a token");

        log.info("Auth token received: {}", response.getJsonField("accessToken"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Response Schema Validation
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify user list response matches expected JSON schema",
            groups = {"api", "schema"})
    public void testUserListSchemaValidation() {
        ApiResponse response = client.get("/users?limit=1");

        String expectedSchema = """
                {
                  "users": [
                    {
                      "id": 1,
                      "firstName": "sample",
                      "lastName": "sample",
                      "age": 28,
                      "email": "sample"
                    }
                  ],
                  "total": 100,
                  "skip": 0,
                  "limit": 1
                }
                """;

        Assert.assertTrue(SchemaValidator.validateStructure(response.getBody(), expectedSchema),
                "User list response should match expected schema structure");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Response Time Check
    // ═══════════════════════════════════════════════════════════════════

    @Test(description = "Verify API response time is within acceptable limits",
            groups = {"api", "performance"})
    public void testResponseTime() {
        ApiResponse response = client.get("/users?limit=5");

        Assert.assertTrue(response.isSuccessful());
        Assert.assertTrue(response.getResponseTimeMs() < 5000,
                "API response time should be under 5 seconds. Actual: " + response.getResponseTimeMs() + "ms");

        log.info("Response time: {}ms", response.getResponseTimeMs());
    }
}
