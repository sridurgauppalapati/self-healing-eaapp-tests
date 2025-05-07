package com.eaapp.tests;

import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class EmployeeTests extends EAAppBaseTest {
    @Test
    public void testCreateNewEmployee() {
    	
        navigateToLoginPage();
        login("admin", "password");
        
        clickElement("EmployeeListLink");
        clickElement("CreateNewLink");
        
        String employeeName = "SriDurga" + System.currentTimeMillis();
        sendKeys("Name", employeeName);
        sendKeys("Salary", "50000");
        // Duration Worked
        int duration = getRandomNumDurationWorked(1, 124);
        clear("DurationWorked");
        sendKeys("DurationWorked",String.valueOf(duration));
        clickElement("CreateButton");
        
        
        // Verify employee was created
        
        Boolean employeeExistence = isEmployeePresent("EmployeeTable", employeeName);
        assertTrue(employeeExistence, "Employee " + employeeName + " found in the table.");
           }
}

