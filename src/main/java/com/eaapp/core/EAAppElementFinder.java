package com.eaapp.core;

import com.eaapp.locators.EAAppLocators;
import com.google.gson.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.json.TypeToken;
import org.openqa.selenium.support.ui.*;
import org.slf4j.*;
import java.io.*;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

public class EAAppElementFinder {
    private static final Logger logger = LoggerFactory.getLogger(EAAppElementFinder.class);
    private WebDriver driver;
    private String openaiApiKey;
    private Map<String, String> healedLocators = new ConcurrentHashMap<>();

    public EAAppElementFinder(WebDriver driver, String openaiApiKey) {
        this.driver = driver;
        this.openaiApiKey = openaiApiKey;
    }

    // Method for finding single element
    public WebElement findElement(String elementKey) {
        String originalLocator = EAAppLocators.LOCATORS.get(elementKey);
        String elementDescription = EAAppLocators.DESCRIPTIONS.get(elementKey);

        if (originalLocator == null) {
            throw new IllegalArgumentException("No locator found for key: " + elementKey);
        }

        try {
            return driver.findElement(By.xpath(originalLocator));
        } catch (NoSuchElementException e) {
            logger.warn("Original locator failed for {}: {}", elementKey, originalLocator);
            return tryHealedLocators(elementKey, originalLocator, elementDescription);
        }
    }

    // Method for finding multiple elements
    public List<WebElement> findElements(String elementKey) {
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
        return tryHealedLocatorsForMultiple(elementKey, originalLocator, elementDescription);
    }

    private WebElement tryHealedLocators(String elementKey, String originalLocator, String elementDescription) {
        String healedLocator = healedLocators.get(elementKey);
        if (healedLocator != null) {
            try {
                return driver.findElement(By.xpath(healedLocator));
            } catch (NoSuchElementException e) {
                logger.warn("Healed locator failed for {}: {}", elementKey, healedLocator);
            }
        }
        return getNewLocatorFromAI(elementKey, originalLocator, elementDescription);
    }

    private List<WebElement> tryHealedLocatorsForMultiple(String elementKey, String originalLocator, String elementDescription) {
        String healedLocator = healedLocators.get(elementKey);
        if (healedLocator != null) {
            List<WebElement> elements = driver.findElements(By.xpath(healedLocator));
            if (!elements.isEmpty()) {
                return elements;
            }
            logger.warn("Healed locator failed for multiple elements {}: {}", elementKey, healedLocator);
        }
        return getNewLocatorsFromAI(elementKey, originalLocator, elementDescription);
    }

    private WebElement getNewLocatorFromAI(String elementKey, String originalLocator, String elementDescription) {
        try {
            String pageSource = driver.getPageSource();
            String newLocator = callAILocatorHealer(pageSource, originalLocator, elementDescription);

            if (newLocator != null) {
                try {
                    WebElement element = driver.findElement(By.xpath(newLocator));
                    healedLocators.put(elementKey, newLocator);
                    logger.info("Successfully healed locator for {}: {}", elementKey, newLocator);
                    return element;
                } catch (NoSuchElementException e) {
                    logger.error("AI-suggested locator failed: {}", newLocator);
                }
            }
        } catch (Exception e) {
            logger.error("Error during AI locator healing", e);
        }
        throw new NoSuchElementException("All attempts to locate element '" + elementKey + "' failed");
    }

    private List<WebElement> getNewLocatorsFromAI(String elementKey, String originalLocator, String elementDescription) {
        try {
            String pageSource = driver.getPageSource();
            String newLocator = callAILocatorHealer(pageSource, originalLocator, elementDescription);

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

    private String callAILocatorHealer(String html, String originalLocator, String description) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.openai.com/v1/chat/completions");

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + openaiApiKey);

        String prompt = String.format(
            "Given this HTML snippet from EAApp:\n%s\n\n" +
            "The original XPath locator '%s' for element '%s' is not working. " +
            "Please suggest 3 alternative reliable XPath locators that would work for this application. " +
            "Focus on attributes that are likely to remain stable. " +
            "Return only a JSON array with the 3 XPath locators.",
            html, originalLocator, description);

        String requestBody = String.format(
            "{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"temperature\": 0.5}",
            prompt);

        try {
            httpPost.setEntity(new StringEntity(requestBody));
            CloseableHttpResponse response = httpClient.execute(httpPost);

            String responseBody = EntityUtils.toString(response.getEntity());
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            String content = jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            JsonArray locators = JsonParser.parseString(content).getAsJsonArray();
            return locators.get(0).getAsString(); // Return the first suggested locator
        } catch (Exception e) {
            logger.error("Error calling AI API", e);
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
            new Gson().toJson(healedLocators, writer);
            logger.info("Saved healed locators to {}", filePath);
        } catch (IOException e) {
            logger.error("Error saving healed locators", e);
        }
    }

    public void loadHealedLocatorsFromFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Map<String, String> loaded = new Gson().fromJson(reader, 
                new TypeToken<Map<String, String>>(){}.getType());
            if (loaded != null) {
                healedLocators.putAll(loaded);
                logger.info("Loaded {} healed locators from {}", loaded.size(), filePath);
            }
        } catch (IOException e) {
            logger.error("Error loading healed locators", e);
        }
    }
}
