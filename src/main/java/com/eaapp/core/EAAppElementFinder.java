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
    private WebDriver driver;
    private String openaiApiKey;
    private Map<String, String> healedLocators = new ConcurrentHashMap<>();

    public EAAppElementFinder(WebDriver driver, String openaiApiKey) {
        this.driver = driver;
        this.openaiApiKey = ConfigReader.getProperty("openai.api.key");
    }

    // Method for finding single element
    public WebElement findElement(String elementKey) {
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
                    logger.error("AI-suggested locator failed validation: {}", newLocator);
                    // healedLocators.put(elementKey + "_failed", newLocator);
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
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", String.format(
            "Given this HTML snippet from EAApp (http://eaapp.somee.com/):\n%s\n\n" +
                "The original XPath locator \"%s\" for element \"%s\" is not working. " +
                "Please suggest EXACTLY 3 alternative XPath locators with these rules:\n" +
                "1. Use contains() for partial matching\n" +
                "2. Combine multiple attributes when possible\n" +
                "3. Return ONLY a JSON array with 3 XPaths\n" +
                "Example formats:\n" +
                "- //a[contains(@href,\"Login\")]\n" +
                "- //input[@type=\"submit\" and contains(@value,\"Log in\")]\n" +
                "- //input[contains(@id,\"Salary\") or @name=\"Salary\"]",
                html, originalLocator, description));
        messages.add(message);
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
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();
    
            // 6. Validate the response format
            try {
                JsonArray locators = JsonParser.parseString(content).getAsJsonArray();
                if (locators.size() > 0) {
                    return locators.get(0).getAsString();
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
