package framework.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatModel;
import framework.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core OpenAI integration service — the brain of the AI-native framework.
 *
 * <p>This service manages the OpenAI API client lifecycle, handles retries with
 * exponential backoff, and provides a clean interface for sending prompts and
 * receiving structured JSON responses.</p>
 *
 * <p><b>Design Decision:</b> We use the official OpenAI Java SDK (com.openai:openai-java)
 * rather than raw HTTP calls. This gives us type-safe request building, automatic
 * serialization, and built-in error handling.</p>
 *
 * <p><b>Why not mocked?</b> The assignment explicitly requires "a real LLM API call,
 * not mocked." This service always calls the real OpenAI API.</p>
 */
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 1000;

    private static volatile AIService instance;
    private final OpenAIClient client;
    private final String model;
    private final int maxTokens;
    private final boolean enabled;

    private AIService() {
        this.enabled = ConfigReader.getBoolean("ai.enabled");
        this.model = ConfigReader.get("ai.model");
        this.maxTokens = ConfigReader.getInt("ai.maxTokens");

        if (enabled) {
            String apiKey = ConfigReader.getOpenAiApiKey();
            this.client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
            log.info("AIService initialized — model={}, maxTokens={}", model, maxTokens);
        } else {
            this.client = null;
            log.info("AIService disabled via config (ai.enabled=false)");
        }
    }

    /**
     * Thread-safe singleton — one client instance shared across all test threads.
     */
    public static AIService getInstance() {
        if (instance == null) {
            synchronized (AIService.class) {
                if (instance == null) {
                    instance = new AIService();
                }
            }
        }
        return instance;
    }

    /**
     * Sends a prompt to the LLM and returns the raw text response.
     *
     * @param systemPrompt  The system-level instruction (role, constraints)
     * @param userPrompt    The user-level content (actual data to analyze)
     * @return Raw response text from the LLM
     */
    public String analyze(String systemPrompt, String userPrompt) {
        if (!enabled) {
            log.warn("AI analysis skipped — ai.enabled=false");
            return "{\"error\": \"AI service is disabled\"}";
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.debug("Sending AI analysis request (attempt {}/{})", attempt, MAX_RETRIES);

                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model(ChatModel.of(model))
                        .maxCompletionTokens(maxTokens)
                        .addSystemMessage(systemPrompt)
                        .addUserMessage(userPrompt)
                        .build();

                ChatCompletion completion = client.chat().completions().create(params);

                String response = completion.choices().get(0).message().content().orElse("");
                log.debug("AI response received — {} chars", response.length());
                return response;

            } catch (Exception e) {
                log.error("AI API call failed (attempt {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    sleep(BASE_DELAY_MS * (long) Math.pow(2, attempt - 1));
                } else {
                    log.error("All {} AI API attempts exhausted", MAX_RETRIES);
                    return "{\"error\": \"AI API call failed after " + MAX_RETRIES + " attempts: " + e.getMessage() + "\"}";
                }
            }
        }
        return "{\"error\": \"Unexpected state\"}";
    }

    /**
     * Sends a prompt and parses the JSON response into a typed object.
     *
     * @param systemPrompt  System instruction
     * @param userPrompt    User content
     * @param responseType  Target class for JSON deserialization
     * @return Parsed response object, or null on failure
     */
    public <T> T analyzeAndParse(String systemPrompt, String userPrompt, Class<T> responseType) {
        String rawResponse = analyze(systemPrompt, userPrompt);
        try {
            // Strip markdown code fences if present (LLM sometimes wraps JSON in ```json ... ```)
            String cleaned = cleanJsonResponse(rawResponse);
            return objectMapper.readValue(cleaned, responseType);
        } catch (Exception e) {
            log.error("Failed to parse AI response to {}: {}", responseType.getSimpleName(), e.getMessage());
            log.debug("Raw AI response was: {}", rawResponse);
            return null;
        }
    }

    /**
     * Checks if the AI service is enabled and ready.
     */
    public boolean isEnabled() {
        return enabled && client != null;
    }

    /**
     * Strips markdown code fences from LLM responses.
     * LLMs sometimes wrap JSON in ```json ... ``` even when asked not to.
     */
    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";
        String trimmed = response.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
