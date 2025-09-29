package com.example.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ApiTest extends BaseTest {
    
    @Test(priority = 1)
    public void testGetAllPosts() {
        logTestInfo("Test Get All Posts");
        
        given()
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].title", notNullValue())
                .body("[0].body", notNullValue())
                .body("[0].userId", notNullValue());
    }
    
    @Test(priority = 2)
    public void testGetSinglePost() {
        logTestInfo("Test Get Single Post");
        
        int postId = 1;
        
        given()
                .pathParam("id", postId)
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .body("title", notNullValue())
                .body("body", notNullValue())
                .body("userId", notNullValue());
    }
    
    @Test(priority = 3)
    public void testCreatePost() {
        logTestInfo("Test Create Post");
        
        String requestBody = "{\n" +
                "    \"title\": \"Test Post Title\",\n" +
                "    \"body\": \"Test Post Body Content\",\n" +
                "    \"userId\": 1\n" +
                "}";
        
        Response response = given()
                .body(requestBody)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title", equalTo("Test Post Title"))
                .body("body", equalTo("Test Post Body Content"))
                .body("userId", equalTo(1))
                .body("id", notNullValue())
                .extract().response();
        
        int createdPostId = response.jsonPath().getInt("id");
        System.out.println("Created post with ID: " + createdPostId);
    }
    
    @Test(priority = 4)
    public void testUpdatePost() {
        logTestInfo("Test Update Post");
        
        int postId = 1;
        String requestBody = "{\n" +
                "    \"id\": 1,\n" +
                "    \"title\": \"Updated Post Title\",\n" +
                "    \"body\": \"Updated Post Body Content\",\n" +
                "    \"userId\": 1\n" +
                "}";
        
        given()
                .pathParam("id", postId)
                .body(requestBody)
                .when()
                .put("/posts/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(postId))
                .body("title", equalTo("Updated Post Title"))
                .body("body", equalTo("Updated Post Body Content"))
                .body("userId", equalTo(1));
    }
    
    @Test(priority = 5)
    public void testDeletePost() {
        logTestInfo("Test Delete Post");
        
        int postId = 1;
        
        given()
                .pathParam("id", postId)
                .when()
                .delete("/posts/{id}")
                .then()
                .statusCode(200);
    }
    
    @Test(priority = 6, enabled = false)
    public void testGetNonExistentPost() {
        logTestInfo("Test Get Non-Existent Post");
        
        // Note: This test is disabled as JSONPlaceholder behavior may vary
        // You can enable and adjust based on your actual API behavior
        given()
                .pathParam("id", 999999)
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(404);
    }
}