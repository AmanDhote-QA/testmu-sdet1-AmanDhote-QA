package framework.ai;

/**
 * Captures the full context of a test failure for AI analysis.
 * This rich context enables the LLM to provide accurate root-cause analysis
 * rather than generic suggestions.
 */
public class FailureContext {

    private String testName;
    private String testClass;
    private String testMethod;
    private String exceptionType;
    private String exceptionMessage;
    private String stackTrace;
    private String pageUrl;
    private String pageTitle;
    private String domSnapshot;
    private String consoleLogs;
    private String screenshotPath;
    private long executionDurationMs;
    private int retryCount;
    private String browser;
    private String environment;

    public FailureContext() {
    }

    // ── Getters & Setters ──

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getDomSnapshot() {
        return domSnapshot;
    }

    public void setDomSnapshot(String domSnapshot) {
        this.domSnapshot = domSnapshot;
    }

    public String getConsoleLogs() {
        return consoleLogs;
    }

    public void setConsoleLogs(String consoleLogs) {
        this.consoleLogs = consoleLogs;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "FailureContext{" +
                "testName='" + testName + '\'' +
                ", testClass='" + testClass + '\'' +
                ", exceptionType='" + exceptionType + '\'' +
                ", pageUrl='" + pageUrl + '\'' +
                '}';
    }
}
