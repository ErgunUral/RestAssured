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
    
    @BeforeClass
    public void setUp() {
        // Base URI configuration
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
        
        // Request specification
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
        
        // Response specification
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
        
        // Set default specifications
        RestAssured.requestSpecification = requestSpec;
        RestAssured.responseSpecification = responseSpec;
    }
    
    protected void logTestInfo(String testName) {
        System.out.println("\n=== " + testName + " ===");
    }
}