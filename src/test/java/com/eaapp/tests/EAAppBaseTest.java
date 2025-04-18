// EAAppBaseTest.java
package com.eaapp.tests;

import com.eaapp.core.EAAppElementFinder;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EAAppBaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(EAAppBaseTest.class);
    protected WebDriver driver;
    protected EAAppElementFinder elementFinder;
    private static final String HEALED_LOCATORS_FILE = "com/eaapp/resources/healed_locators.json";
    
    @BeforeSuite
    public void beforeSuite() {
        WebDriverManager.chromedriver().setup();
    }
    
    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        // Initialize with your OpenAI API key
        elementFinder = new EAAppElementFinder(driver, "your-openai-api-key");
        
        // Load previously healed locators
        elementFinder.loadHealedLocatorsFromFile(HEALED_LOCATORS_FILE);
    }
    
    @AfterClass
    public void tearDown() {
        if (driver != null) {
            // Save healed locators before quitting
            elementFinder.saveHealedLocatorsToFile(HEALED_LOCATORS_FILE);
            driver.quit();
        }
    }
    
    protected void navigateToLoginPage() {
        driver.get("http://eaapp.somee.com/");
        clickElement("LoginLink");
    }
    
    protected void login(String username, String password) {
        sendKeys("UserName", username);
        sendKeys("Password", password);
        clickElement("LoginButton");
    }
    
    protected void clickElement(String elementKey) {
        elementFinder.findElement(elementKey).click();
    }
    
    protected void clear(String elementKey) {
        elementFinder.findElement(elementKey).clear();
    }
    
    protected void sendKeys(String elementKey, String text) {
        elementFinder.findElement(elementKey).sendKeys(text);
    }
    
    protected String getElementText(String elementKey) {
        return elementFinder.findElement(elementKey).getText();
    }
    protected int getRandomNumDurationWorked(int minHours, int maxHours) {
        Random random = new Random();
        return random.nextInt(maxHours - minHours + 1) + minHours;
    }
    protected boolean isEmployeePresent(String elements, String expectedText) {
        List<WebElement> employeeNameList = elementFinder.findElements(elements);
        
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
