package com.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.Map;

public class TestUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Convert JSON string to Map
     */
    public static Map<String, Object> jsonToMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to Map", e);
        }
    }
    
    /**
     * Convert object to JSON string
     */
    public static String objectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
    
    /**
     * Print response details for debugging
     */
    public static void printResponse(Response response) {
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Time: " + response.getTime() + "ms");
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
    }
    
    /**
     * Generate random email
     */
    public static String generateRandomEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }
    
    /**
     * Generate random string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}