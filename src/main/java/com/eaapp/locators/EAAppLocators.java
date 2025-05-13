package com.eaapp.locators;

import java.util.HashMap;
import java.util.Map;

public class EAAppLocators {
 public static final Map<String, String> LOCATORS = new HashMap<>();
 public static final Map<String, String> DESCRIPTIONS = new HashMap<>();
 
 static {
     // Login Page
     LOCATORS.put("LoginLink", "//a[@id='loginLink']");
     DESCRIPTIONS.put("LoginLink", "Login navigation link in header");
     
     LOCATORS.put("UserName", "//input[@id='UserName']");
     DESCRIPTIONS.put("UserName", "Username input field on login page");
     
     LOCATORS.put("Password", "//input[@id='Password']");
     DESCRIPTIONS.put("Password", "Password input field on login page");
     
     LOCATORS.put("LoginButton", "//input[@value='Log in']");
     DESCRIPTIONS.put("LoginButton", "Login submit button");
     
     LOCATORS.put("LogoutButton", "//form[@id='logoutForm']");
     DESCRIPTIONS.put("LogoutButton", "Logout button");
     
     // Employee Page
     LOCATORS.put("EmployeeListLink", "//a[contains(text(),'Employee List')]");
     DESCRIPTIONS.put("EmployeeListLink", "Employee List navigation link");
     
     LOCATORS.put("CreateNewLink", "//a[contains(text(),'Create New')]");
     DESCRIPTIONS.put("CreateNewLink", "Create New employee link");
     
     LOCATORS.put("Name", "//input[@id='Name']");
     DESCRIPTIONS.put("Name", "Employee name input field");
     
     LOCATORS.put("Salary", "//input[@id='Salary']");
     DESCRIPTIONS.put("Salary", "Employee salary input field");
     
     LOCATORS.put("DurationWorked", "//input[@id='DurationWorked']");
     DESCRIPTIONS.put("DurationWorked", "Duration Worked input field");
     
     LOCATORS.put("CreateButton", "//input[@value='Create']");
     DESCRIPTIONS.put("CreateButton", "Create employee submit button");
     
     LOCATORS.put("EmployeeTable", "//table//tbody/tr/td[1]");
     DESCRIPTIONS.put("EmployeeTable", "Employee Table");
     
     // Search functionality
     LOCATORS.put("SearchBox", "//input[@id='SearchString']");
     DESCRIPTIONS.put("SearchBox", "Search input field on employee list page");
     
     LOCATORS.put("SearchButton", "//input[@value='Search']");
     DESCRIPTIONS.put("SearchButton", "Search button on employee list page");
     
     LOCATORS.put("EmployeeTableRows", "//table[@class='table']//tbody/tr");
     DESCRIPTIONS.put("EmployeeTableRows", "All rows in the employee table");
     
     LOCATORS.put("NoResultMessage", "//div[contains(text(),'No match')]");
     DESCRIPTIONS.put("NoResultMessage", "No search results message");
     
     // Employee Details and Actions
     LOCATORS.put("BenefitsLink", "//a[contains(text(),'Benefits')]");
     DESCRIPTIONS.put("BenefitsLink", "Benefits link for an employee");
     
     LOCATORS.put("EditLink", "//a[contains(text(),'Edit')]");
     DESCRIPTIONS.put("EditLink", "Edit link for an employee");
     
     LOCATORS.put("DeleteLink", "//a[contains(text(),'Delete')]");
     DESCRIPTIONS.put("DeleteLink", "Delete link for an employee");
     
     LOCATORS.put("BenefitsPageHeader", "//h2[contains(text(),'Benefits')]");
     DESCRIPTIONS.put("BenefitsPageHeader", "Header on the Benefits page");
     
     LOCATORS.put("BenefitsEmployeeName", "//dt[contains(text(),'Employee')]/following-sibling::dd[1]");
     DESCRIPTIONS.put("BenefitsEmployeeName", "Employee name on benefits page");
     
     // Edit Employee Page
     LOCATORS.put("EditPageHeader", "//h2[contains(text(),'Edit')]");
     DESCRIPTIONS.put("EditPageHeader", "Header on the Edit Employee page");
     
     LOCATORS.put("SaveButton", "//input[@value='Save']");
     DESCRIPTIONS.put("SaveButton", "Save button on edit employee page");
     
     LOCATORS.put("BackToListLink", "//a[contains(text(),'Back to List')]");
     DESCRIPTIONS.put("BackToListLink", "Back to List link on edit employee page");
     
     LOCATORS.put("Grade", "//input[@id='Grade']");
     DESCRIPTIONS.put("Grade", "Grade input field on employee form");
     
     LOCATORS.put("Email", "//input[@id='Email']");
     DESCRIPTIONS.put("Email", "Email input field on employee form");
     
     // Delete Employee Page
     LOCATORS.put("DeletePageHeader", "//h2[contains(text(),'Delete')]");
     DESCRIPTIONS.put("DeletePageHeader", "Header on the Delete Employee page");
     
     LOCATORS.put("DeleteConfirmButton", "//input[@value='Delete']");
     DESCRIPTIONS.put("DeleteConfirmButton", "Delete confirmation button");
     
     LOCATORS.put("DeleteEmployeeDetails", "//dl[@class='dl-horizontal']");
     DESCRIPTIONS.put("DeleteEmployeeDetails", "Employee details on delete confirmation page");
 }
}