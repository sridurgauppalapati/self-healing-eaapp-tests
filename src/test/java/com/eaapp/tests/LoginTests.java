
package com.eaapp.tests;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LoginTests extends EAAppBaseTest {
   
	@Test
    public void testSuccessfulLogin() {
        navigateToLoginPage();
        login("admin", "password");
        
        // Verify login by checking for logout link
        assertTrue(elementFinder.findElement(getDriver(),"//form[@id='logoutForm']").isDisplayed());
    }
    
//    @Test
//    public void testInvalidLogin() {
//        navigateToLoginPage();
//        login("invalid", "credentials");
//        
//        // Verify error message
//        String errorText = elementFinder.findElement("//div[contains(@class,'validation-summary-errors')]").getText();
//        assertTrue(errorText.contains("Invalid login attempt"));
//    }
}

