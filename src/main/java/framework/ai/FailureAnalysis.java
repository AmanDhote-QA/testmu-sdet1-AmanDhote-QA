package framework.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Structured AI response for a single test failure analysis.
 * The LLM is forced to respond in this exact JSON schema via response_format,
 * ensuring machine-parseable output instead of freeform text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FailureAnalysis {

    @JsonProperty("root_cause")
    private String rootCause;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("suggested_fix")
    private String suggestedFix;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("category")
    private String category;

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("test_name")
    private String testName;

    public FailureAnalysis() {
    }

    // ── Getters & Setters ──

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    @Override
    public String toString() {
        return "FailureAnalysis{" +
                "rootCause='" + rootCause + '\'' +
                ", severity='" + severity + '\'' +
                ", category='" + category + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
