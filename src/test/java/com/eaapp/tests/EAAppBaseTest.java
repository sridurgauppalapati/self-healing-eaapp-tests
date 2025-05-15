
// EAAppBaseTest.java
package com.eaapp.tests;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.eaapp.core.EAAppElementFinder;
import com.eaapp.utils.ConfigReader;

import io.github.bonigarcia.wdm.WebDriverManager;


public class EAAppBaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(EAAppBaseTest.class);
    private static WebDriver driver;  // private static
    private static final Object lock = new Object();
    protected EAAppElementFinder elementFinder;
    private static boolean suiteInitialized = false;

    private static final String HEALED_LOCATORS_FILE = "src/main/resources/healed_locators.json";
    
    @BeforeSuite
    public void beforeSuite() {
        synchronized (lock) {
            if (!suiteInitialized) {
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();
                driver.manage().window().maximize();
                elementFinder = new EAAppElementFinder(driver, ConfigReader.getProperty("openai.api.key"));
                elementFinder.loadHealedLocatorsFromFile(HEALED_LOCATORS_FILE);
                suiteInitialized = true;
                
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (driver != null) {
                        elementFinder.saveHealedLocatorsToFile(HEALED_LOCATORS_FILE);
                        driver.quit();
                    }
                }));
            }
        }
    }
    
    @BeforeClass
    public void setUp() {
        // No need to initialize driver here anymore
    }
    
    @AfterClass
    public void tearDown() {
        // Don't quit the driver here - let @AfterSuite handle it
    }
    
    @AfterSuite
    public void afterSuite() {
        synchronized (lock) {
            if (driver != null) {
                try {
                    elementFinder.saveHealedLocatorsToFile(HEALED_LOCATORS_FILE);
                    logger.info("Saving healed locators and closing browser");
                } catch (Exception e) {
                    logger.error("Error while saving healed locators", e);
                }
                driver.quit();
                driver = null;
                suiteInitialized = false;
            }
        }
    }
    
    // Helper method to access driver
    protected WebDriver getDriver() {
        return driver;
    }
    protected void navigateToLoginPage() {
        getDriver().get(ConfigReader.getProperty("app.url"));
        clickElement("LoginLink");
    }
    
    protected void login(String username, String password) {
        sendKeys("UserName", username);
        sendKeys("Password", password);
        clickElement("LoginButton");
    }
    
    protected void clickElement(String elementKey) {
        elementFinder.findElement(getDriver(), elementKey).click();
    }
    
    protected void clear(String elementKey) {
        elementFinder.findElement(getDriver(),elementKey).clear();
    }
    
    protected void sendKeys(String elementKey, String text) {
        elementFinder.findElement(getDriver(),elementKey).sendKeys(text);
    }
    
    protected String getElementText(String elementKey) {
        return elementFinder.findElement(getDriver(),elementKey).getText();
    }
    protected int getRandomNumDurationWorked(int minHours, int maxHours) {
        Random random = new Random();
        return random.nextInt(maxHours - minHours + 1) + minHours;
    }
    protected boolean isEmployeePresent(String elements, String expectedText) {
        List<WebElement> employeeNameList = elementFinder.findElements(getDriver(),elements);
        
        if (employeeNameList.isEmpty()) {
            logger.info("No employees found in the list");
            return false;
        }

        boolean found = false;
        for (WebElement element : employeeNameList) {
            String employeeName = element.getText().trim();
            logger.debug("Checking employee name: {}", employeeName);
            
            if (employeeName.equals(expectedText)) {
                found = true;
                logger.info("Employee found in the list: {}", expectedText);
                break;
            }
        }

        if (!found) {
            logger.info("Employee not found in the list: {}", expectedText);
        }
        
        return found;
    }

}
