package com.eaapp.tests;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class EmployeeTests extends EAAppBaseTest {
    @Test(dependsOnMethods = "testSuccessfulLogin")
    public void testCreateNewEmployee() {
        navigateToLoginPage();
        login("admin", "password");
        
        clickElement("EmployeeListLink");
        clickElement("CreateNewLink");
        
        String employeeName = "John Doe " + System.currentTimeMillis();
        sendKeys("Name", employeeName);
        sendKeys("Salary", "50000");
        clickElement("CreateButton");
        
        // Verify employee was created
        String successMessage = elementFinder.findElement("//div[contains(@class,'alert-success')]").getText();
        assertTrue(successMessage.contains("Create Successful"));
    }
}

