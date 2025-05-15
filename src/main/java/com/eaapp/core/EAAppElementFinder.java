package com.eaapp.core;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.json.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaapp.locators.EAAppLocators;
import com.eaapp.utils.ConfigReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EAAppElementFinder {
    private static final Logger logger = LoggerFactory.getLogger(EAAppElementFinder.class);
    // private final WebDriver driver;
    private final String openaiApiKey;
    private Map<String, String> healedLocators = new ConcurrentHashMap<>();

    public EAAppElementFinder(WebDriver driver, String openaiApiKey) {
        // this.driver = driver;
        this.openaiApiKey = ConfigReader.getProperty("openai.api.key");
    }

    // Method for finding single element
    public WebElement findElement(WebDriver driver,String elementKey) {
        String originalLocator = EAAppLocators.LOCATORS.get(elementKey);
        String elementDescription = EAAppLocators.DESCRIPTIONS.get(elementKey);

        if (originalLocator == null) {
            throw new IllegalArgumentException("No locator found for key: " + elementKey);
        }

        logger.info("Attempting to find element with key: {} using locator: {}", elementKey, originalLocator);

        try {
            logger.info("Entering try block for element: {}", elementKey);
            WebElement element = driver.findElement(By.xpath(originalLocator));
            logger.info("Element found successfully: {}", elementKey);
            return element;
        } catch (Exception e) {
            logger.warn("Caught NoSuchElementException for {}: {}", elementKey, originalLocator);
            logger.warn("Exception details: ", e);
            return tryHealedLocators(driver, elementKey, originalLocator, elementDescription);

        }
    }

    // Method for finding multiple elements
    public List<WebElement> findElements(WebDriver driver, String elementKey) {
        String originalLocator = EAAppLocators.LOCATORS.get(elementKey);
        String elementDescription = EAAppLocators.DESCRIPTIONS.get(elementKey);

        if (originalLocator == null) {
            throw new IllegalArgumentException("No locator found for key: " + elementKey);
        }

        List<WebElement> elements = driver.findElements(By.xpath(originalLocator));
        if (!elements.isEmpty()) {
            return elements;
        }

        logger.warn("Original locator failed to find elements for {}: {}", elementKey, originalLocator);
        return tryHealedLocatorsForMultiple(driver, elementKey, originalLocator, elementDescription);
    }

    private WebElement tryHealedLocators(WebDriver driver,String elementKey, String originalLocator, String elementDescription) {
        String healedLocator = healedLocators.get(elementKey);
        if (healedLocator != null) {
            try {
                return driver.findElement(By.xpath(healedLocator));
            } catch (NoSuchElementException e) {
                logger.warn("Healed locator failed for {}: {}", elementKey, healedLocator);
            }
        }
        return getNewLocatorFromAI(driver, elementKey, originalLocator, elementDescription);
    }

    private List<WebElement> tryHealedLocatorsForMultiple(WebDriver driver,String elementKey, String originalLocator,
            String elementDescription) {
        String healedLocator = healedLocators.get(elementKey);
        if (healedLocator != null) {
            List<WebElement> elements = driver.findElements(By.xpath(healedLocator));
            if (!elements.isEmpty()) {
                return elements;
            }
            logger.warn("Healed locator failed for multiple elements {}: {}", elementKey, healedLocator);
        }
        return getNewLocatorsFromAI(driver, elementKey, originalLocator, elementDescription);
    }

    private WebElement getNewLocatorFromAI(WebDriver driver, String elementKey, String originalLocator, String elementDescription) {
    try {
        String pageSource = driver.getPageSource();
        String newLocator = callAILocatorHealer(driver, pageSource, originalLocator, elementDescription);

        if (newLocator != null) {
            // Since callAILocatorHealer already verified this locator works, we can use it directly
            WebElement element = driver.findElement(By.xpath(newLocator));
            healedLocators.put(elementKey, newLocator);
            logger.info("Successfully healed locator for {}: {}", elementKey, newLocator);
            return element;
        } else {
            logger.error("AI could not suggest any working locator for {}", elementKey);
        }
    } catch (Exception e) {
        logger.error("Error during AI locator healing", e);
    }
    throw new NoSuchElementException("All attempts to locate element '" + elementKey + "' failed");
}


    private List<WebElement> getNewLocatorsFromAI(WebDriver driver, String elementKey, String originalLocator,
            String elementDescription) {
        try {
            String pageSource = driver.getPageSource();
            String newLocator = callAILocatorHealer(driver, pageSource, originalLocator, elementDescription);

            if (newLocator != null) {
                List<WebElement> elements = driver.findElements(By.xpath(newLocator));
                if (!elements.isEmpty()) {
                    healedLocators.put(elementKey, newLocator);
                    logger.info("Successfully healed locator for multiple elements {}: {}", elementKey, newLocator);
                    return elements;
                }
                logger.error("AI-suggested locator failed to find elements: {}", newLocator);
            }
        } catch (Exception e) {
            logger.error("Error during AI locator healing for multiple elements", e);
        }
        return Collections.emptyList();
    }

    private String callAILocatorHealer(WebDriver driver, String html, String originalLocator, String description) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 1. Use correct API endpoint (was using api key as URL)
        HttpPost httpPost = new HttpPost("https://api.openai.com/v1/chat/completions");

        // 2. Properly format headers
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + ConfigReader.getProperty("openai.api.key"));
        httpPost.setHeader("Accept", "application/json");

        // 3. Create properly structured JSON payload
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4o-mini"); // Fixed model name (was gpt-4o-mini)
        payload.addProperty("temperature", 0.7);

        JsonArray messages = new JsonArray();

        // Add system message to control output format
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content",
                "You are a specialized XPath generator that only outputs raw JSON arrays. " +
                        "Never use markdown formatting, code blocks, or explanatory text. " +
                        "Your response must be a valid JSON array containing exactly 3 XPath strings, nothing else.");
        messages.add(systemMessage);

        // Add user message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", String.format(
                "Given this HTML snippet from EAApp (http://eaapp.somee.com/):\n%s\n\n" +
                        "The original XPath locator \"%s\" for element \"%s\" is not working. " +
                        "Generate EXACTLY 3 alternative XPath locators with these rules:\n" +
                        "1. Use contains() for partial matching\n" +
                        "2. Combine multiple attributes when possible\n" +
                        "3. Return ONLY a raw JSON array with 3 XPaths without any markdown formatting\n" +
                        "Example formats for individual XPaths:\n" +
                        "//a[contains(@href,\"Login\")]\n" +
                        "//input[@type=\"submit\" and contains(@value,\"Log in\")]\n" +
                        "//input[contains(@id,\"Salary\") or @name=\"Salary\"]",
                html, originalLocator, description));
        messages.add(userMessage);
        payload.add("messages", messages);

        try {
            // 4. Properly encode the JSON payload
            StringEntity entity = new StringEntity(payload.toString(), "UTF-8");
            httpPost.setEntity(entity);

            logger.debug("Sending OpenAI request: {}", payload);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());

            logger.debug("OpenAI response: {}", responseBody);

            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            // 5. Handle potential API errors
            if (jsonResponse.has("error")) {
                JsonObject error = jsonResponse.getAsJsonObject("error");
                logger.error("OpenAI API error: {}", error.get("message").getAsString());
                return null;
            }

            String content = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            // 6. Validate the response format
            // 6. Validate the response format and try each locator
            try {
                JsonArray locators = JsonParser.parseString(content).getAsJsonArray();
                if (locators.size() > 0) {
                    for (int i = 0; i < locators.size(); i++) {
                        String locator = locators.get(i).getAsString();
                        try {
                            // Try to find element with this locator
                            WebElement element = driver.findElement(By.xpath(locator));
                            if (element != null) {
                                logger.info("Successfully found element with AI-suggested locator #{}: {}", i + 1,
                                        locator);
                                return locator;
                            }
                        } catch (NoSuchElementException e) {
                            logger.warn("AI-suggested locator #{} failed: {}", i + 1, locator);
                            // Continue to next locator
                        }
                    }
                    logger.error("All AI-suggested locators failed");
                }
            } catch (Exception e) {
                logger.error("Failed to parse OpenAI response content", e);
            }
            return null;

        } catch (Exception e) {
            logger.error("Error calling OpenAI API", e);
            return null;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Error closing HTTP client", e);
            }
        }
    }

    public void saveHealedLocatorsToFile(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
            gson.toJson(healedLocators, writer);
            logger.info("Saved healed locators to {}", filePath);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error saving healed locators", e);
        }
    }

    public void loadHealedLocatorsFromFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Map<String, String> loaded = new Gson().fromJson(reader,
                    new TypeToken<Map<String, String>>() {
                    }.getType());
            if (loaded != null) {
                healedLocators.putAll(loaded);
                logger.info("Loaded {} healed locators from {}", loaded.size(), filePath);
            }
        } catch (IOException e) {
            logger.error("Error loading healed locators", e);
        }
    }
}