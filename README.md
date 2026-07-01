# TestMu SDET-1 Assignment

Welcome to my submission for the TestMu SDET-1 assignment! 

This repository contains a modern, enterprise-grade test automation framework built from scratch using Java, Selenium, and TestNG. 

While building this, I didn't want to just submit a standard Page Object Model framework. I wanted to build something truly unique. To do that, I've integrated **OpenAI as a first-class citizen** directly into the test execution lifecycle.

## What makes this framework different?

The assignment asked to choose between two AI integration options (Option A: Failure Explainer, or Option B: Flaky Test Classifier). I decided to implement **both** to create a complete AI-Native testing experience:

1. **AI Failure Explainer:** Whenever a test fails, a custom TestNG listener automatically captures the full context (DOM state, console logs, stack trace, screenshot path) and sends it to the LLM. You get an immediate, actionable root cause analysis.
2. **Flaky Test Classifier:** After the test suite finishes running, all the failures are gathered and fed into the AI Engine again. The LLM holistically analyzes the run and categorizes failures into Real Bugs, Environment Issues, or Flaky Tests.

All of this data is exported into a beautiful, dark-themed HTML report located in `reports/llm-output/`.

## Framework Architecture

- **Thread-Safe Execution:** Parallel execution is supported out of the box using `ThreadLocal` for WebDriver instances inside `DriverManager`.
- **JSON-Driven Locators:** Locators aren't hardcoded in Java classes. They are completely externalized into JSON files under `src/main/resources/locators/`, making them much easier to maintain.
- **Lightweight API Testing:** Instead of pulling in heavy dependencies like RestAssured, API tests use a custom lightweight `RestClient` built on top of Java 11's native `HttpClient`.
- **Robust Configuration:** Settings are managed through `config.properties` and structured logging is handled via SLF4J/Logback.

## Project Structure

```text
src/
├── main/
│   ├── java/framework/
│   │   ├── ai/          # Core LLM integration and prompt engineering
│   │   ├── api/         # Lightweight REST client and schema validator
│   │   ├── config/      # Properties reader
│   │   ├── driver/      # ThreadLocal driver management
│   │   ├── listeners/   # TestNG listeners (Screenshot, Retry, AI)
│   │   ├── pages/       # Core Page Objects
│   │   └── reporting/   # Custom HTML + JSON report generator
│   └── resources/
│       ├── config/      # Framework properties
│       ├── locators/    # JSON locator files
│       └── logback.xml  # Logging config
└── test/
    ├── java/tests/      # Test classes (Login, Dashboard, API)
    └── resources/       
        └── suites/      # TestNG XML suites
```

## Prerequisites

Before running the framework, make sure you have the following installed:
- **Java JDK 17** (or higher)
- **Gradle** (The project includes a Gradle wrapper, so you can just use `./gradlew`)
- **OpenAI API Key** (Required for the AI features to work)

## Setup & Execution

1. **Set your OpenAI API Key:**
   Export your API key as an environment variable so the framework can pick it up.
   ```bash
   # On Windows (PowerShell)
   $env:OPENAI_API_KEY="your-api-key-here"
   
   # On Mac/Linux
   export OPENAI_API_KEY="your-api-key-here"
   ```

2. **Run the tests:**
   You can run the entire regression suite via the command line using the Gradle wrapper:
   ```bash
   ./gradlew clean test
   ```
   *(Note: The first run might take a moment as it downloads dependencies like the OpenAI SDK and Selenium).*

3. **View the Reports:**
   Once the test execution completes, check the `reports/llm-output/` directory for the AI-generated HTML report. It includes standard metrics alongside the AI's deep analysis and classifications.

## What I'd Build Next With More Time

If I had more time to expand this framework, I would build:
1. **AI Auto-Healing Locators:** Instead of just explaining why a test failed, the AI could analyze the new DOM structure and automatically propose (or even inject) an updated JSON locator file during execution.
2. **Parallel Cross-Browser Execution Grid:** Hooking up `DriverFactory` to a cloud grid (like BrowserStack or LambdaTest) to run the AI analysis simultaneously across Safari, Chrome, and Edge.
3. **CI/CD Integration:** Wiring the framework into GitHub Actions with automated PR comments, so the AI Flaky Test Classifier automatically blocks PRs if it detects a high likelihood of new flaky code.
4. **Historical Flakiness Trends:** Storing the `Flaky Classifier` results in a database (like PostgreSQL) to track which test classes degrade over time.
