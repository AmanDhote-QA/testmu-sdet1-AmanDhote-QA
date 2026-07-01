package framework.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Analysis Engine — Strategy-pattern orchestrator for LLM-powered test analysis.
 *
 * <p>This engine is what makes the framework truly unique. Instead of picking either
 * Option A (Failure Explainer) or Option B (Flaky Test Classifier), we implement
 * <b>both</b> and let them work together:</p>
 *
 * <ol>
 *   <li><b>Per-failure analysis</b> (Option A): Each failing test gets an individual
 *       AI-powered root cause analysis with suggested fixes</li>
 *   <li><b>Suite-level classification</b> (Option B): After the entire suite runs,
 *       all failures are fed to the LLM for holistic classification into
 *       real_bugs / environment_issues / flaky_tests</li>
 * </ol>
 *
 * <p><b>Why both?</b> Option A gives developers immediate, actionable feedback per test.
 * Option B gives QA leads a high-level triage view. Together they provide complete
 * coverage of the failure analysis workflow.</p>
 *
 * <p><b>Design inspiration:</b> The reference frameworks use plugin/observer patterns
 * (PluginObservable, ResultListener) for extensibility. We adopt a similar strategy
 * pattern but purpose-built for AI analysis rather than generic event handling.</p>
 */
public class AIAnalysisEngine {

    private static final Logger log = LoggerFactory.getLogger(AIAnalysisEngine.class);
    private final AIService aiService;

    public AIAnalysisEngine() {
        this.aiService = AIService.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OPTION A — Failure Explainer (per-test analysis)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Analyzes a single test failure and returns a structured explanation.
     * Called by AITestListener.onTestFailure() for each failing test.
     *
     * @param context  The full failure context (exception, page state, etc.)
     * @return FailureAnalysis with root cause, explanation, suggested fix
     */
    public FailureAnalysis explainFailure(FailureContext context) {
        if (!aiService.isEnabled()) {
            log.warn("AI disabled — skipping failure explanation for: {}", context.getTestName());
            return createFallbackAnalysis(context);
        }

        log.info("🔍 AI analyzing failure: {}", context.getTestName());

        String systemPrompt = buildFailureExplainerSystemPrompt();
        String userPrompt = buildFailureExplainerUserPrompt(context);

        FailureAnalysis analysis = aiService.analyzeAndParse(systemPrompt, userPrompt, FailureAnalysis.class);

        if (analysis == null) {
            log.warn("AI returned unparseable response — using fallback analysis");
            return createFallbackAnalysis(context);
        }

        analysis.setTestName(context.getTestName());
        log.info("✅ AI analysis complete for: {} — root cause: {}", context.getTestName(), analysis.getRootCause());
        return analysis;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OPTION B — Flaky Test Classifier (suite-level analysis)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Classifies all failures from a test suite run into three buckets:
     * real_bugs, environment_issues, flaky_tests.
     * Called by AITestListener.onFinish() after the suite completes.
     *
     * @param failures  List of all failure contexts from the suite run
     * @return ClassificationReport with bucketed failures
     */
    public ClassificationReport classifyFailures(List<FailureContext> failures) {
        if (!aiService.isEnabled()) {
            log.warn("AI disabled — skipping flaky test classification");
            return createFallbackReport(failures);
        }

        if (failures == null || failures.isEmpty()) {
            log.info("No failures to classify");
            return createEmptyReport();
        }

        log.info("🔬 AI classifying {} failures into buckets...", failures.size());

        String systemPrompt = buildClassifierSystemPrompt();
        String userPrompt = buildClassifierUserPrompt(failures);

        ClassificationReport report = aiService.analyzeAndParse(systemPrompt, userPrompt, ClassificationReport.class);

        if (report == null) {
            log.warn("AI returned unparseable classification — using fallback");
            return createFallbackReport(failures);
        }

        report.setTotalFailures(failures.size());
        log.info("✅ Classification complete — {} real bugs, {} env issues, {} flaky",
                report.getRealBugs() != null ? report.getRealBugs().size() : 0,
                report.getEnvironmentIssues() != null ? report.getEnvironmentIssues().size() : 0,
                report.getFlakyTests() != null ? report.getFlakyTests().size() : 0);

        return report;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Prompt Engineering — Carefully crafted prompts for structured output
    // ═══════════════════════════════════════════════════════════════════════

    private String buildFailureExplainerSystemPrompt() {
        return """
                You are an expert test failure analyst for a Selenium-based test automation framework.
                Your job is to analyze test failures and provide actionable insights.
                
                You MUST respond in valid JSON with exactly this schema:
                {
                  "root_cause": "concise 1-2 sentence root cause",
                  "explanation": "plain English explanation of what broke and why (3-5 sentences)",
                  "suggested_fix": "specific, actionable steps to fix the issue",
                  "severity": "CRITICAL | HIGH | MEDIUM | LOW",
                  "category": "LOCATOR_ISSUE | TIMING_ISSUE | DATA_ISSUE | ENV_ISSUE | APP_BUG | NETWORK_ERROR",
                  "confidence": 0.0 to 1.0
                }
                
                Guidelines:
                - Be specific, not generic. Reference actual element names, URLs, and error messages.
                - For NoSuchElementException: check if the locator is correct, if the page loaded, if there's a timing issue.
                - For TimeoutException: suggest explicit waits or check if the application is slow.
                - For AssertionError: compare expected vs actual values and suggest data fixes.
                - Confidence should reflect how certain you are about the root cause.
                """;
    }

    private String buildFailureExplainerUserPrompt(FailureContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TEST FAILURE CONTEXT ===\n\n");
        sb.append("Test: ").append(ctx.getTestClass()).append(".").append(ctx.getTestMethod()).append("\n");
        sb.append("Exception: ").append(ctx.getExceptionType()).append("\n");
        sb.append("Message: ").append(ctx.getExceptionMessage()).append("\n\n");

        if (ctx.getStackTrace() != null) {
            // Trim stack trace to relevant framework lines
            String trimmed = trimStackTrace(ctx.getStackTrace(), 20);
            sb.append("Stack Trace (relevant lines):\n").append(trimmed).append("\n\n");
        }

        if (ctx.getPageUrl() != null) {
            sb.append("Page URL: ").append(ctx.getPageUrl()).append("\n");
        }
        if (ctx.getPageTitle() != null) {
            sb.append("Page Title: ").append(ctx.getPageTitle()).append("\n");
        }

        if (ctx.getDomSnapshot() != null && !ctx.getDomSnapshot().isEmpty()) {
            // Limit DOM to 3000 chars to stay within token limits
            String dom = ctx.getDomSnapshot().length() > 3000
                    ? ctx.getDomSnapshot().substring(0, 3000) + "\n... [truncated]"
                    : ctx.getDomSnapshot();
            sb.append("\nDOM Snapshot:\n").append(dom).append("\n");
        }

        if (ctx.getConsoleLogs() != null && !ctx.getConsoleLogs().isEmpty()) {
            sb.append("\nBrowser Console Logs:\n").append(ctx.getConsoleLogs()).append("\n");
        }

        sb.append("\nExecution Duration: ").append(ctx.getExecutionDurationMs()).append("ms\n");
        sb.append("Retry Count: ").append(ctx.getRetryCount()).append("\n");
        sb.append("Browser: ").append(ctx.getBrowser()).append("\n");
        sb.append("Environment: ").append(ctx.getEnvironment()).append("\n");

        return sb.toString();
    }

    private String buildClassifierSystemPrompt() {
        return """
                You are an expert test failure classifier for a test automation suite.
                Given a list of test failures from a single test run, classify each failure into exactly one bucket:
                
                1. REAL_BUG — The failure indicates an actual bug in the application under test
                2. ENVIRONMENT_ISSUE — The failure is caused by test environment problems (network, server, config)
                3. FLAKY_TEST — The failure is intermittent and likely caused by timing, race conditions, or test instability
                
                You MUST respond in valid JSON with exactly this schema:
                {
                  "real_bugs": [{"test_name": "...", "classification": "REAL_BUG", "reasoning": "...", "confidence": 0.0-1.0, "exception_type": "..."}],
                  "environment_issues": [{"test_name": "...", "classification": "ENVIRONMENT_ISSUE", "reasoning": "...", "confidence": 0.0-1.0, "exception_type": "..."}],
                  "flaky_tests": [{"test_name": "...", "classification": "FLAKY_TEST", "reasoning": "...", "confidence": 0.0-1.0, "exception_type": "..."}],
                  "summary": "1-2 sentence overall summary"
                }
                
                Classification heuristics:
                - TimeoutException with retry > 0 → likely FLAKY_TEST
                - AssertionError with specific value mismatch → likely REAL_BUG
                - ConnectionException, 503/502 errors → likely ENVIRONMENT_ISSUE
                - NoSuchElementException on a page that loaded → likely REAL_BUG (locator changed)
                - NoSuchElementException on a page that didn't load → likely ENVIRONMENT_ISSUE
                - Intermittent StaleElementReferenceException → likely FLAKY_TEST
                """;
    }

    private String buildClassifierUserPrompt(List<FailureContext> failures) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TEST SUITE FAILURE REPORT ===\n");
        sb.append("Total failures: ").append(failures.size()).append("\n\n");

        for (int i = 0; i < failures.size(); i++) {
            FailureContext f = failures.get(i);
            sb.append("--- Failure #").append(i + 1).append(" ---\n");
            sb.append("Test: ").append(f.getTestClass()).append(".").append(f.getTestMethod()).append("\n");
            sb.append("Exception: ").append(f.getExceptionType()).append("\n");
            sb.append("Message: ").append(f.getExceptionMessage()).append("\n");
            sb.append("Duration: ").append(f.getExecutionDurationMs()).append("ms\n");
            sb.append("Retry Count: ").append(f.getRetryCount()).append("\n");
            if (f.getPageUrl() != null) {
                sb.append("Page URL: ").append(f.getPageUrl()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Fallback methods when AI is unavailable
    // ═══════════════════════════════════════════════════════════════════════

    private FailureAnalysis createFallbackAnalysis(FailureContext ctx) {
        FailureAnalysis analysis = new FailureAnalysis();
        analysis.setTestName(ctx.getTestName());
        analysis.setRootCause("AI analysis unavailable — manual review required");
        analysis.setExplanation("Exception: " + ctx.getExceptionType() + " — " + ctx.getExceptionMessage());
        analysis.setSuggestedFix("Review the stack trace and page state manually");
        analysis.setSeverity("UNKNOWN");
        analysis.setCategory("UNKNOWN");
        analysis.setConfidence(0.0);
        return analysis;
    }

    private ClassificationReport createFallbackReport(List<FailureContext> failures) {
        ClassificationReport report = new ClassificationReport();
        report.setTotalFailures(failures.size());
        report.setSummary("AI classification unavailable — all failures listed as unclassified");
        report.setRealBugs(List.of());
        report.setEnvironmentIssues(List.of());
        report.setFlakyTests(List.of());
        return report;
    }

    private ClassificationReport createEmptyReport() {
        ClassificationReport report = new ClassificationReport();
        report.setTotalFailures(0);
        report.setSummary("No failures to classify — all tests passed");
        report.setRealBugs(List.of());
        report.setEnvironmentIssues(List.of());
        report.setFlakyTests(List.of());
        return report;
    }

    /**
     * Trims stack trace to the most relevant lines.
     */
    private String trimStackTrace(String stackTrace, int maxLines) {
        String[] lines = stackTrace.split("\n");
        if (lines.length <= maxLines) return stackTrace;

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            if (count >= maxLines) {
                sb.append("    ... ").append(lines.length - maxLines).append(" more lines\n");
                break;
            }
            sb.append(line).append("\n");
            count++;
        }
        return sb.toString();
    }
}
