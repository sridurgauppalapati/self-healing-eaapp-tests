package com.eaapp.locators;


import java.util.HashMap;
import java.util.Map;

public class EAAppLocators {
 public static final Map<String, String> LOCATORS = new HashMap<>();
 public static final Map<String, String> DESCRIPTIONS = new HashMap<>();
 
 static {
     // Login Page
     LOCATORS.put("LoginLink", "//a[@id='loginLink1']");
     DESCRIPTIONS.put("LoginLink", "Login navigation link in header");
     
     LOCATORS.put("UserName", "//input[@id='UserName']");
     DESCRIPTIONS.put("UserName", "Username input field on login page");
     
     LOCATORS.put("Password", "//input[@id='Password']");
     DESCRIPTIONS.put("Password", "Password input field on login page");
     
     LOCATORS.put("LoginButton", "//input[@value='Log in']");
     DESCRIPTIONS.put("LoginButton", "Login submit button");
     
     LOCATORS.put("LogoutButton", "//form[@id='logoutForm'']");
     DESCRIPTIONS.put("LoginButton", "Logout button");
     
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
 }
}
