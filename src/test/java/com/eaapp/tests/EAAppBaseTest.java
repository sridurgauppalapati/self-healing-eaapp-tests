// EAAppBaseTest.java
package com.eaapp.tests;

import com.eaapp.core.EAAppElementFinder;
import org.openqa.selenium.WebDriver;
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
    
    protected void sendKeys(String elementKey, String text) {
        elementFinder.findElement(elementKey).sendKeys(text);
    }
    
    protected String getElementText(String elementKey) {
        return elementFinder.findElement(elementKey).getText();
    }
}
