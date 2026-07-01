package framework.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Structured classification report from the Flaky Test Classifier (Option B).
 * After a test suite run, the LLM analyzes all failures and sorts them into
 * three buckets: real_bugs, environment_issues, and flaky_tests.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassificationReport {

    @JsonProperty("real_bugs")
    private List<ClassifiedFailure> realBugs;

    @JsonProperty("environment_issues")
    private List<ClassifiedFailure> environmentIssues;

    @JsonProperty("flaky_tests")
    private List<ClassifiedFailure> flakyTests;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("total_failures")
    private int totalFailures;

    public ClassificationReport() {
    }

    // ── Getters & Setters ──

    public List<ClassifiedFailure> getRealBugs() {
        return realBugs;
    }

    public void setRealBugs(List<ClassifiedFailure> realBugs) {
        this.realBugs = realBugs;
    }

    public List<ClassifiedFailure> getEnvironmentIssues() {
        return environmentIssues;
    }

    public void setEnvironmentIssues(List<ClassifiedFailure> environmentIssues) {
        this.environmentIssues = environmentIssues;
    }

    public List<ClassifiedFailure> getFlakyTests() {
        return flakyTests;
    }

    public void setFlakyTests(List<ClassifiedFailure> flakyTests) {
        this.flakyTests = flakyTests;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getTotalFailures() {
        return totalFailures;
    }

    public void setTotalFailures(int totalFailures) {
        this.totalFailures = totalFailures;
    }

    /**
     * Represents a single classified failure within a bucket.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClassifiedFailure {

        @JsonProperty("test_name")
        private String testName;

        @JsonProperty("classification")
        private String classification;

        @JsonProperty("reasoning")
        private String reasoning;

        @JsonProperty("confidence")
        private double confidence;

        @JsonProperty("exception_type")
        private String exceptionType;

        public ClassifiedFailure() {
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }

        public String getReasoning() {
            return reasoning;
        }

        public void setReasoning(String reasoning) {
            this.reasoning = reasoning;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }
    }
}
