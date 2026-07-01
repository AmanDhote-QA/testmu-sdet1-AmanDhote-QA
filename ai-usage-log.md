# AI Usage Log

This document tracks all AI tools utilized during the development of this framework, as required by the TestMu SDET-1 assignment.

| AI Tool Used | Task It Helped With | What It Produced |
|--------------|---------------------|------------------|
| **Claude 3.5 Sonnet (via Antigravity IDE)** | Framework Scaffolding (Task 1) | Generated the initial Gradle setup, POM architecture, `DriverFactory`, and `ConfigReader`. |
| **Claude 3.5 Sonnet (via Antigravity IDE)** | Prompt Engineering & Test Case Generation (Task 2) | Acted as the LLM interface to generate the test cases for Login, Dashboard, and API modules. The raw prompts are saved in `prompts.md`. |
| **OpenAI API (gpt-4o-mini)** | Failure Explainer & Flaky Test Classifier (Task 3) | Provided the real-time AI API integration inside the TestNG listeners (`AITestListener`, `AIAnalysisEngine`). |
| **Claude 3.5 Sonnet (via Antigravity IDE)** | Custom Reporting | Generated the `AIReportGenerator.java` class with CSS/HTML to parse the OpenAI API output into a visually appealing structured test report. |
| **Claude 3.5 Sonnet (via Antigravity IDE)** | API Test Refactoring | Helped migrate `ApiTests.java` from the `reqres.in` API to `dummyjson.com` when `reqres.in` suddenly required an authentication key during development. |
| **Claude 3.5 Sonnet (via Antigravity IDE)** | Documentation | Formatted this `README.md`, the `ai-usage-log.md`, and the overall markdown structure of the repository. |
