# Prompt Engineering for Test Case Generation

> **Task 2 Deliverable** — Every prompt used, exactly as written. No cleaning up after the fact.

---

## Module 1: Login

### Prompt 1.1 — Initial Login Test Generation

```
I am building a Selenium + TestNG test automation framework in Java targeting the login page at
https://the-internet.herokuapp.com/login. The page has:
- Username field (id="username")
- Password field (id="password")
- Login button (button type="submit")
- Flash message div (id="flash") that shows success or error messages

Valid credentials: username="tomsmith", password="SuperSecretPassword!"

Generate comprehensive TestNG test cases covering:
1. Valid login with correct credentials
2. Invalid credentials (wrong username, wrong password, both wrong)
3. Empty field validation (empty username, empty password, both empty)
4. Forgot password link flow
5. Session expiry — accessing /secure without logging in first
6. Brute-force lockout — 5 rapid failed attempts

Use @DataProvider for the invalid credential scenarios. Each test should have a descriptive
@Test annotation with description and groups. Use assertions to verify expected behavior.
Output as Java code that extends a BaseTest class.
```

### Prompt 1.2 — Refinement (after first iteration didn't include session expiry properly)

```
The session expiry test needs refinement. On https://the-internet.herokuapp.com, when you access
/secure directly without a session, the server doesn't return a 401 — it redirects you back to /login.

Update the test to:
1. Navigate directly to /secure (without logging in first)
2. Assert that the current URL contains "login" (confirming redirect happened)
3. This simulates what happens when a session expires or a user tries to bypass authentication

Also, the brute-force test should navigate back to /login between attempts since the flash
message persists on the page.
```

### What didn't work — Login Module (3-5 lines)

The first prompt generated a session expiry test that expected a 401 HTTP status code, but
the-internet.herokuapp.com uses a server-side redirect (302) back to `/login` instead. The
test was asserting the wrong thing. I refined the prompt to check the redirect URL instead.
The brute-force test also initially didn't navigate back between attempts, causing the login
form to not reset properly. Added explicit navigation between attempts.

---

## Module 2: Dashboard

### Prompt 2.1 — Dashboard Test Generation

```
I need TestNG test cases for a dashboard/secure area page at https://the-internet.herokuapp.com/secure.
After successful login (username="tomsmith", password="SuperSecretPassword!"), the user lands on
this page which has:
- Heading (h2 element) containing "Secure Area"
- Flash message (id="flash") showing "You logged into a secure area!"
- Logout button (a.button with href="/logout")
- Subheader (h4.subheader)

Generate test cases covering:
1. Widget/content loading — verify dashboard heading appears after login
2. Data accuracy — verify heading text contains "Secure Area"
3. Welcome message verification
4. Permission-based visibility — logout button visible only when logged in
5. Unauthorized access — accessing /secure after logout should redirect to /login
6. Logout flow — clicking logout returns to login page
7. URL verification — dashboard URL contains "/secure"

Each test should:
- Login in @BeforeMethod (using a LoginPage and DashboardPage page objects)
- Use descriptive @Test annotations with groups
- Have clear assertions with failure messages
```

### Prompt 2.2 — Refinement (unauthorized access test)

```
The "unauthorized access redirect" test needs to first logout, THEN try to access /secure again.
The current implementation tries to access /secure without logging out first, which doesn't test
the actual scenario. Update the test to:
1. Log out from the dashboard
2. Navigate to /secure directly
3. Assert redirect to /login page
```

### What didn't work — Dashboard Module (3-5 lines)

The initial dashboard tests didn't properly handle the login prerequisite — the LLM generated
tests that assumed you were already on the dashboard without performing login first. I had to
explicitly specify that login should happen in @BeforeMethod. Also, the unauthorized access
test was confusing — it was testing the same scenario as the login session expiry test. I
refined it to test the specific flow of logout → attempt to access secure area → verify redirect.

---

## Module 3: REST API

### Prompt 3.1 — API Test Generation

```
Generate TestNG test cases for REST API testing against https://reqres.in/api using Java's
built-in HttpClient (java.net.http). No RestAssured — using a custom RestClient class that
returns an ApiResponse object with getStatusCode(), getBody(), asJson(), getJsonField(name),
getResponseTimeMs(), and isSuccessful() methods.

Cover these scenarios:
1. GET /users?page=1 — verify 200 status, response has "data" array
2. GET /users/2 — verify single user retrieval, check email field exists
3. POST /users — create user with {"name": "TestMu QA Engineer", "job": "SDET-1"}, verify 201
4. PUT /users/2 — update user, verify 200 and updatedAt field
5. DELETE /users/2 — verify 204 No Content
6. GET /users/999999 — verify 404 for non-existent user
7. POST /register with missing password — verify 400 and error message
8. POST /login with invalid credentials — verify 400
9. POST /login with valid credentials (eve.holt@reqres.in / cityslicka) — verify token returned
10. Schema validation — verify user list response structure matches expected fields
11. Response time — verify GET /users returns in under 5 seconds
12. Delayed response — GET /users?delay=2, verify response took >2s

This test class should NOT extend BaseTest or use WebDriver. It should have its own
@BeforeClass to initialize the RestClient and use @Listeners for AITestListener.
Use @DataProvider where applicable. Include groups annotation for each test.
```

### What didn't work — REST API Module (3-5 lines)

The first prompt generated tests using RestAssured syntax (given/when/then), but I specifically
needed tests using my custom RestClient class. I had to re-prompt with the exact RestClient API
method signatures. Also, the schema validation test initially tried to use a JSON Schema Draft-07
library which wasn't in our dependencies. I changed it to use a structural comparison approach
with my custom SchemaValidator that uses Jackson to compare field presence and types.

---

## Prompt Engineering Observations

### What Worked Well
- **Being specific about the target URL and page elements** made the LLM generate accurate locators
- **Providing the exact API of my framework classes** (method signatures, return types) produced usable code
- **Listing specific scenarios** rather than vague requirements gave better coverage
- **Including the test framework annotations** (@Test, @DataProvider, groups) in the prompt guided output format

### What Required Iteration
- **Session management behavior** — LLMs default to standard HTTP status codes (401, 403) but real apps often use redirects
- **Framework-specific APIs** — Without specifying exact method signatures, the LLM invents its own API
- **Test isolation** — Had to explicitly mention @BeforeMethod setup and navigation between test steps
