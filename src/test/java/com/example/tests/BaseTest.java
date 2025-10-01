package com.example.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;

public class BaseTest {
    
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    protected String baseURI = "https://testweb.paytr.com";
    protected String basePath = "";
    
    @BeforeClass
    public void setUp() {
        // PayTR Test Environment Base URI configuration
        RestAssured.baseURI = baseURI;
        
        // Request specification for PayTR
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "PayTR-Test-Automation/1.0")
                .build();
        
        // Response specification
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
        
        // Set default specifications
        RestAssured.requestSpecification = requestSpec;
        RestAssured.responseSpecification = responseSpec;
        
        logTestInfo("PayTR Test Environment initialized: " + baseURI);
    }
    
    protected void logTestInfo(String testName) {
        System.out.println("\n=== " + testName + " ===");
        System.out.println("Test URL: " + baseURI + basePath);
    }
}