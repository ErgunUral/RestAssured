package com.example.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UserApiTest extends BaseTest {
    
    @Test(priority = 1)
    public void testGetAllUsers() {
        logTestInfo("Test Get All Users");
        
        given()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", equalTo(10))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].username", notNullValue())
                .body("[0].email", notNullValue());
    }
    
    @Test(priority = 2)
    public void testGetSingleUser() {
        logTestInfo("Test Get Single User");
        
        int userId = 1;
        
        given()
                .pathParam("id", userId)
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("name", equalTo("Leanne Graham"))
                .body("username", equalTo("Bret"))
                .body("email", equalTo("Sincere@april.biz"))
                .body("address.city", equalTo("Gwenborough"))
                .body("company.name", equalTo("Romaguera-Crona"));
    }
    
    @Test(priority = 3)
    public void testCreateUser() {
        logTestInfo("Test Create User");
        
        String requestBody = "{\n" +
                "    \"name\": \"Test User\",\n" +
                "    \"username\": \"testuser\",\n" +
                "    \"email\": \"test@example.com\",\n" +
                "    \"address\": {\n" +
                "        \"street\": \"Test Street\",\n" +
                "        \"suite\": \"Apt. 123\",\n" +
                "        \"city\": \"Test City\",\n" +
                "        \"zipcode\": \"12345\",\n" +
                "        \"geo\": {\n" +
                "            \"lat\": \"0.0\",\n" +
                "            \"lng\": \"0.0\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"phone\": \"1-555-123-4567\",\n" +
                "    \"website\": \"test.org\",\n" +
                "    \"company\": {\n" +
                "        \"name\": \"Test Company\",\n" +
                "        \"catchPhrase\": \"Test Phrase\",\n" +
                "        \"bs\": \"test business\"\n" +
                "    }\n" +
                "}";
        
        Response response = given()
                .body(requestBody)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test User"))
                .body("username", equalTo("testuser"))
                .body("email", equalTo("test@example.com"))
                .body("id", notNullValue())
                .extract().response();
        
        int createdUserId = response.jsonPath().getInt("id");
        System.out.println("Created user with ID: " + createdUserId);
    }
    
    @Test(priority = 4)
    public void testGetUserPosts() {
        logTestInfo("Test Get User Posts");
        
        int userId = 1;
        
        given()
                .queryParam("userId", userId)
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("findAll { it.userId == " + userId + " }.size()", greaterThan(0));
    }
    
    @Test(priority = 5)
    public void testGetUserAlbums() {
        logTestInfo("Test Get User Albums");
        
        int userId = 1;
        
        given()
                .queryParam("userId", userId)
                .when()
                .get("/albums")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].userId", equalTo(userId))
                .body("[0].id", notNullValue())
                .body("[0].title", notNullValue());
    }
    
    @Test(priority = 6)
    public void testUpdateUser() {
        logTestInfo("Test Update User");
        
        int userId = 1;
        String requestBody = "{\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Updated User Name\",\n" +
                "    \"username\": \"updateduser\",\n" +
                "    \"email\": \"updated@example.com\"\n" +
                "}";
        
        given()
                .pathParam("id", userId)
                .body(requestBody)
                .when()
                .put("/users/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("name", equalTo("Updated User Name"))
                .body("username", equalTo("updateduser"))
                .body("email", equalTo("updated@example.com"));
    }
}