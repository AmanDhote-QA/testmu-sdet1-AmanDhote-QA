package framework.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import framework.ai.ClassificationReport;
import framework.ai.FailureAnalysis;
import framework.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Report Generator — produces rich HTML + JSON reports with AI insights.
 *
 * <p>This is what makes the framework's output truly unique. Instead of plain
 * TestNG HTML reports, we generate:</p>
 * <ol>
 *   <li><b>JSON Report</b>: Machine-readable, with all AI analyses and classifications</li>
 *   <li><b>HTML Report</b>: Styled, with expandable AI analysis panels per failed test</li>
 * </ol>
 *
 * <p>The reports are saved to {@code reports/llm-output/} and include execution
 * metadata, individual failure analyses, and the classification report.</p>
 */
public class AIReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(AIReportGenerator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private AIReportGenerator() {
    }

    /**
     * Generates both JSON and HTML reports with AI analysis results.
     */
    public static void generateReport(List<FailureAnalysis> analyses,
                                       ClassificationReport classificationReport,
                                       ITestContext testContext) {
        String outputDir = ConfigReader.get("report.llmOutputDir");
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

            // Generate JSON report
            generateJsonReport(analyses, classificationReport, testContext, dir, timestamp);

            // Generate HTML report
            generateHtmlReport(analyses, classificationReport, testContext, dir, timestamp);

            log.info("📝 AI reports generated in: {}", dir.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to generate AI report: {}", e.getMessage(), e);
        }
    }

    private static void generateJsonReport(List<FailureAnalysis> analyses,
                                            ClassificationReport classificationReport,
                                            ITestContext testContext,
                                            Path dir, String timestamp) throws IOException {
        Map<String, Object> report = new HashMap<>();

        // Execution metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("suiteName", testContext.getSuite().getName());
        metadata.put("timestamp", timestamp);
        metadata.put("totalPassed", testContext.getPassedTests().size());
        metadata.put("totalFailed", testContext.getFailedTests().size());
        metadata.put("totalSkipped", testContext.getSkippedTests().size());
        metadata.put("browser", ConfigReader.get("browser"));
        metadata.put("environment", ConfigReader.get("environment"));
        metadata.put("aiModel", ConfigReader.get("ai.model"));
        report.put("execution_metadata", metadata);

        // Individual failure analyses
        report.put("failure_analyses", analyses);

        // Classification report
        if (classificationReport != null) {
            report.put("classification_report", classificationReport);
        }

        File jsonFile = dir.resolve("ai-analysis-report_" + timestamp + ".json").toFile();
        objectMapper.writeValue(jsonFile, report);
        log.info("📄 JSON report: {}", jsonFile.getAbsolutePath());
    }

    private static void generateHtmlReport(List<FailureAnalysis> analyses,
                                            ClassificationReport classificationReport,
                                            ITestContext testContext,
                                            Path dir, String timestamp) throws IOException {
        StringBuilder html = new StringBuilder();
        int passed = testContext.getPassedTests().size();
        int failed = testContext.getFailedTests().size();
        int skipped = testContext.getSkippedTests().size();
        int total = passed + failed + skipped;

        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>AI Test Analysis Report — TestMu SDET-1</title>\n");
        html.append("<style>\n");
        html.append(getReportStyles());
        html.append("</style>\n</head>\n<body>\n");

        // Header
        html.append("<div class=\"header\">\n");
        html.append("  <h1>🤖 AI-Powered Test Analysis Report</h1>\n");
        html.append("  <p class=\"subtitle\">TestMu SDET-1 Framework — ").append(timestamp).append("</p>\n");
        html.append("</div>\n");

        // Summary cards
        html.append("<div class=\"summary-grid\">\n");
        html.append(summaryCard("Total Tests", String.valueOf(total), "total"));
        html.append(summaryCard("Passed", String.valueOf(passed), "passed"));
        html.append(summaryCard("Failed", String.valueOf(failed), "failed"));
        html.append(summaryCard("Skipped", String.valueOf(skipped), "skipped"));
        html.append(summaryCard("AI Model", ConfigReader.get("ai.model"), "ai"));
        html.append(summaryCard("Browser", ConfigReader.get("browser"), "browser"));
        html.append("</div>\n");

        // Classification Report (Option B)
        if (classificationReport != null && classificationReport.getTotalFailures() > 0) {
            html.append("<div class=\"section\">\n");
            html.append("<h2>🔬 Flaky Test Classification (Option B)</h2>\n");
            html.append("<p class=\"summary-text\">").append(escapeHtml(classificationReport.getSummary())).append("</p>\n");

            html.append("<div class=\"classification-grid\">\n");
            html.append(classificationBucket("🐛 Real Bugs", classificationReport.getRealBugs(), "bug"));
            html.append(classificationBucket("🌐 Environment Issues", classificationReport.getEnvironmentIssues(), "env"));
            html.append(classificationBucket("🎲 Flaky Tests", classificationReport.getFlakyTests(), "flaky"));
            html.append("</div>\n");
            html.append("</div>\n");
        }

        // Individual Failure Analyses (Option A)
        if (analyses != null && !analyses.isEmpty()) {
            html.append("<div class=\"section\">\n");
            html.append("<h2>🔍 Individual Failure Analyses (Option A)</h2>\n");

            for (FailureAnalysis analysis : analyses) {
                html.append("<div class=\"analysis-card\">\n");
                html.append("<div class=\"analysis-header\">\n");
                html.append("  <h3>").append(escapeHtml(analysis.getTestName())).append("</h3>\n");
                html.append("  <span class=\"severity severity-").append(safeStr(analysis.getSeverity()).toLowerCase()).append("\">")
                        .append(safeStr(analysis.getSeverity())).append("</span>\n");
                html.append("</div>\n");

                html.append("<div class=\"analysis-body\">\n");
                html.append("<div class=\"field\"><strong>Root Cause:</strong> ").append(escapeHtml(analysis.getRootCause())).append("</div>\n");
                html.append("<div class=\"field\"><strong>Explanation:</strong> ").append(escapeHtml(analysis.getExplanation())).append("</div>\n");
                html.append("<div class=\"field suggestion\"><strong>💡 Suggested Fix:</strong> ").append(escapeHtml(analysis.getSuggestedFix())).append("</div>\n");
                html.append("<div class=\"field-row\">\n");
                html.append("  <span class=\"tag\">Category: ").append(safeStr(analysis.getCategory())).append("</span>\n");
                html.append("  <span class=\"tag\">Confidence: ").append(String.format("%.0f%%", analysis.getConfidence() * 100)).append("</span>\n");
                html.append("</div>\n");
                html.append("</div>\n");
                html.append("</div>\n");
            }
            html.append("</div>\n");
        }

        // Footer
        html.append("<div class=\"footer\">\n");
        html.append("  <p>Generated by TestMu SDET-1 AI-Native Framework | Powered by OpenAI ").append(ConfigReader.get("ai.model")).append("</p>\n");
        html.append("</div>\n");

        html.append("</body>\n</html>");

        File htmlFile = dir.resolve("ai-analysis-report_" + timestamp + ".html").toFile();
        try (FileWriter writer = new FileWriter(htmlFile)) {
            writer.write(html.toString());
        }
        log.info("🌐 HTML report: {}", htmlFile.getAbsolutePath());
    }

    // ── HTML Helpers ──

    private static String summaryCard(String label, String value, String type) {
        return "<div class=\"card card-" + type + "\">\n" +
                "  <div class=\"card-value\">" + escapeHtml(value) + "</div>\n" +
                "  <div class=\"card-label\">" + escapeHtml(label) + "</div>\n" +
                "</div>\n";
    }

    private static String classificationBucket(String title,
                                                List<ClassificationReport.ClassifiedFailure> failures,
                                                String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"bucket bucket-").append(type).append("\">\n");
        sb.append("  <h3>").append(title).append(" (").append(failures != null ? failures.size() : 0).append(")</h3>\n");
        if (failures != null && !failures.isEmpty()) {
            sb.append("  <ul>\n");
            for (ClassificationReport.ClassifiedFailure f : failures) {
                sb.append("    <li><strong>").append(escapeHtml(f.getTestName())).append("</strong>");
                sb.append(" — ").append(escapeHtml(f.getReasoning()));
                sb.append(" <em>(confidence: ").append(String.format("%.0f%%", f.getConfidence() * 100)).append(")</em></li>\n");
            }
            sb.append("  </ul>\n");
        } else {
            sb.append("  <p class=\"empty\">None</p>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "N/A";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String safeStr(String s) {
        return s != null ? s : "UNKNOWN";
    }

    private static String getReportStyles() {
        return """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Segoe UI', system-ui, -apple-system, sans-serif; background: #0f0f23; color: #e0e0e0; line-height: 1.6; }
                .header { background: linear-gradient(135deg, #1a1a3e 0%, #2d1b69 100%); padding: 2rem; text-align: center; border-bottom: 3px solid #7c3aed; }
                .header h1 { font-size: 2rem; color: #fff; }
                .header .subtitle { color: #a5b4fc; margin-top: 0.5rem; }
                .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 1rem; padding: 1.5rem; max-width: 1200px; margin: 0 auto; }
                .card { background: #1e1e3f; border-radius: 12px; padding: 1.5rem; text-align: center; border: 1px solid #333; transition: transform 0.2s; }
                .card:hover { transform: translateY(-2px); }
                .card-value { font-size: 2rem; font-weight: 700; }
                .card-label { font-size: 0.85rem; color: #888; margin-top: 0.25rem; }
                .card-passed .card-value { color: #4ade80; }
                .card-failed .card-value { color: #f87171; }
                .card-skipped .card-value { color: #fbbf24; }
                .card-total .card-value { color: #60a5fa; }
                .card-ai .card-value { color: #c084fc; font-size: 1rem; }
                .card-browser .card-value { color: #67e8f9; font-size: 1rem; }
                .section { max-width: 1200px; margin: 1.5rem auto; padding: 0 1.5rem; }
                .section h2 { color: #c084fc; margin-bottom: 1rem; font-size: 1.5rem; }
                .summary-text { color: #a5b4fc; margin-bottom: 1rem; font-style: italic; }
                .classification-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1rem; }
                .bucket { background: #1e1e3f; border-radius: 12px; padding: 1.5rem; border-left: 4px solid; }
                .bucket-bug { border-color: #f87171; }
                .bucket-env { border-color: #fbbf24; }
                .bucket-flaky { border-color: #60a5fa; }
                .bucket h3 { margin-bottom: 0.75rem; }
                .bucket ul { list-style: none; }
                .bucket li { padding: 0.5rem 0; border-bottom: 1px solid #333; }
                .bucket li:last-child { border-bottom: none; }
                .bucket .empty { color: #666; font-style: italic; }
                .analysis-card { background: #1e1e3f; border-radius: 12px; margin-bottom: 1rem; overflow: hidden; border: 1px solid #333; }
                .analysis-header { display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.5rem; background: #252550; }
                .analysis-header h3 { color: #e0e0e0; font-size: 1rem; }
                .severity { padding: 0.25rem 0.75rem; border-radius: 20px; font-size: 0.75rem; font-weight: 600; }
                .severity-critical { background: #dc2626; color: #fff; }
                .severity-high { background: #f97316; color: #fff; }
                .severity-medium { background: #eab308; color: #000; }
                .severity-low { background: #22c55e; color: #fff; }
                .severity-unknown { background: #6b7280; color: #fff; }
                .analysis-body { padding: 1.5rem; }
                .field { margin-bottom: 0.75rem; }
                .suggestion { background: #1a2e1a; border-left: 3px solid #4ade80; padding: 0.75rem; border-radius: 4px; }
                .field-row { display: flex; gap: 0.75rem; margin-top: 0.75rem; }
                .tag { background: #333; padding: 0.25rem 0.75rem; border-radius: 12px; font-size: 0.8rem; }
                .footer { text-align: center; padding: 2rem; color: #666; font-size: 0.85rem; border-top: 1px solid #333; margin-top: 2rem; }
                """;
    }
}
