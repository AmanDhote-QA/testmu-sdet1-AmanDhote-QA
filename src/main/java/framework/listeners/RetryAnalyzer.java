package framework.listeners;

import framework.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry Analyzer — retries failed tests to detect flakiness.
 *
 * <p>When a test fails, this analyzer retries it up to a configurable count.
 * The retry count is stored in the test result attributes so the Flaky Test
 * Classifier (Option B) can use it as a signal: tests that pass on retry
 * are strong candidates for the FLAKY_TEST bucket.</p>
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetry = ConfigReader.getInt("retry.maxCount");
        if (retryCount < maxRetry) {
            retryCount++;
            result.setAttribute("retryCount", retryCount);
            log.warn("🔄 Retrying test: {}.{} (attempt {}/{})",
                    result.getTestClass().getRealClass().getSimpleName(),
                    result.getMethod().getMethodName(),
                    retryCount, maxRetry);
            return true;
        }
        return false;
    }
}
