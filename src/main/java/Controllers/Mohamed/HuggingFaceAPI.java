package controllers.Mohamed;

import okhttp3.*;
import java.io.IOException;

public class HuggingFaceAPI {

    private static final String API_URL = "https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english";
    private static final String API_TOKEN = "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";

    public String determinePriority(String description) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Create JSON payload
        String json = "{\"inputs\": \"" + description + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // Debug: Print the JSON payload
        System.out.println("JSON Payload: " + json);

        // Create request
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .build();

        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API Error: " + response.code() + " - " + response.message());
            }

            // Parse response
            String responseBody = response.body().string();
            System.out.println("API Response: " + responseBody); // Debug: Print the API response
            return parsePriorityFromResponse(responseBody, description); // Pass description to the parsing method
        }
    }
    private String parsePriorityFromResponse(String responseBody, String description) {
        // Debug: Print the response body for verification
        System.out.println("Parsing Response: " + responseBody);

        // Custom logic for critical issues
        String lowerDesc = description.toLowerCase();
        if (lowerDesc.contains("fails") || lowerDesc.contains("crashes") || lowerDesc.contains("urgent") ||
                lowerDesc.contains("critical") || lowerDesc.contains("blocking") || lowerDesc.contains("unusable")) {
            System.out.println("Critical issue detected. Setting priority to Haute.");
            return "Haute";
        }

        // Parse the JSON response
        try {
            // Use a JSON library like Gson or Jackson for parsing
            // Here, we'll use simple string manipulation for demonstration
            if (responseBody.contains("\"label\":\"NEGATIVE\"")) {
                System.out.println("Detected NEGATIVE label. Setting priority to Faible.");
                return "Faible"; // Map NEGATIVE to LOW priority
            } else if (responseBody.contains("\"label\":\"POSITIVE\"")) {
                System.out.println("Detected POSITIVE label. Setting priority to Haute.");
                return "Haute"; // Map POSITIVE to HIGH priority
            } else {
                System.out.println("No label detected. Setting priority to Moyenne.");
                return "Moyenne"; // Default to MEDIUM priority
            }
        } catch (Exception e) {
            System.out.println("Error parsing API response: " + e.getMessage());
            return "Moyenne"; // Default to MEDIUM priority in case of errors
        }
    }
}