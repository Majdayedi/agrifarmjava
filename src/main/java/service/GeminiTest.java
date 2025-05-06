package service;

import entite.Field;
import entite.Task;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.sql.Timestamp;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class GeminiTest {
    private static final String GEMINI_API_KEY = "AIzaSyC4YArqhQwxx7DuMfkUT9KHxuWglwfvpqc";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
    private static final TaskService taskService = new TaskService();

    public void generator(Field field, String description,java.util.Date startDate) {
        try {
            System.out.println("Testing with description: " + description);

            // Get current date as starting point

            List<Task> tasks = generateFarmingTasks(field, description, startDate);
            if (tasks != null && !tasks.isEmpty()) {
                System.out.println("Generated " + tasks.size() + " tasks:");
                for (Task task : tasks) {
                    System.out.println("\nTask: " + task);
                }
            } else {
                System.out.println("Failed to generate tasks");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Task> generateFarmingTasks(Field field, String description, java.util.Date startDate) {
        try {
            System.out.println("Starting task generation for field: " + field.getId());

            // Create a comprehensive farming task prompt
            String prompt = String.format(
                    "You are an expert farming task manager. Generate a detailed sequence of tasks for the following farming activity: '%s'. " +
                            "Consider the following aspects:\n" +
                            "1. Farm Management:\n" +
                            "   - Soil preparation and maintenance\n" +
                            "   - Irrigation systems\n" +
                            "   - Pest control\n" +
                            "   - Equipment maintenance\n" +
                            "2. Resource Management:\n" +
                            "   - Labor requirements\n" +
                            "   - Equipment needs\n" +
                            "   - Material requirements\n" +
                            "   - Budget considerations\n" +
                            "3. Safety and Compliance:\n" +
                            "   - Safety protocols\n" +
                            "   - Environmental regulations\n" +
                            "   - Quality standards\n" +
                            "4. Timeline and Dependencies:\n" +
                            "   - Task sequencing\n" +
                            "   - Critical path\n" +
                            "   - Seasonal considerations\n" +
                            "5.  tratement based on the discease\n" +
                            "For each task, provide:\n" +
                            "- A clear, specific short task name\n" +
                            "- Brief description of the work (max 255 characters)\n" +
                            "- Required resources and materials\n" +
                            "- Number of workers needed\n" +
                            "- Estimated duration (in days)\n" +
                            "- Priority level (High/Medium/Low)\n" +
                            "- Payment per worker\n" +
                            "- Responsible person (max 50 characters)\n" +
                            "- Dependencies on other tasks\n" +
                            "\nReturn the response as a JSON array of tasks, each in this format:\n" +
                            "{\"name\":\"task name\",\"description\":\"brief description\",\"priority\":\"High/Medium/Low\"," +
                            "\"estimatedDuration\":\"number of days\",\"workers\":number,\"paymentWorker\":number," +
                            "\"ressource\":\"required resources\",\"responsable\":\"short name\",\"dependencies\":[\"task1\",\"task2\"]}",
                    description
            );

            System.out.println("Sending prompt to Gemini: " + prompt);

            String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt.replace("\"", "\\\"") + "\" }] }] }";
            System.out.println("Request body: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Gemini API Response Status: " + response.statusCode());
            System.out.println("Gemini API Response Body: " + response.body());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Full response body: " + responseBody);

                // Parse the response using JSONObject
                JSONObject jsonResponse = new JSONObject(responseBody);
                String text = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                System.out.println("Extracted text: " + text);

                // Find the JSON array in the text
                int startIndex = text.indexOf('[');
                int endIndex = text.lastIndexOf(']') + 1;

                if (startIndex >= 0 && endIndex > startIndex) {
                    String jsonArray = text.substring(startIndex, endIndex);
                    System.out.println("Extracted JSON array: " + jsonArray);

                    // Parse the tasks JSON array
                    JSONArray tasksJson = new JSONArray(jsonArray);
                    List<Task> tasks = new ArrayList<>();

                    // Track the current date for task sequencing
                    long currentDateMillis = startDate.getTime();

                    for (int i = 0; i < tasksJson.length(); i++) {
                        JSONObject taskJson = tasksJson.getJSONObject(i);

                        // Limit description length to 255 characters
                        String taskDescription = taskJson.optString("description", description);
                        if (taskDescription.length() > 255) {
                            taskDescription = taskDescription.substring(0, 252) + "...";
                        }

                        // Limit responsable length to 50 characters
                        String responsable = taskJson.optString("responsable", "Field Manager");
                        if (responsable.length() > 50) {
                            responsable = responsable.substring(0, 47) + "...";
                        }

                        // Parse duration and calculate dates
                        String durationStr = taskJson.optString("estimatedDuration", "1");
                        int durationDays = 1;
                        try {
                            // Extract number from duration string (e.g., "2 days" -> 2)
                            durationStr = durationStr.replaceAll("[^0-9]", "");
                            durationDays = Integer.parseInt(durationStr);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid duration format: " + durationStr);
                        }

                        // Calculate task dates
                        Date taskDate = new Date(currentDateMillis);
                        Date taskDeadline = new Date(currentDateMillis + (durationDays * 24 * 60 * 60 * 1000L));

                        // Move to next task's start date
                        currentDateMillis = taskDeadline.getTime() + (24 * 60 * 60 * 1000L); // Add 1 day buffer between tasks

                        Task task = new Task(
                                field,
                                taskJson.optString("name", "Generated Task"),
                                taskDescription,
                                "to do",
                                taskDate,
                                taskJson.optString("ressource", "TBD"),
                                responsable,
                                taskJson.optString("priority", "Medium"),
                                durationDays + " day" + (durationDays > 1 ? "s" : ""),
                                taskDeadline,
                                taskJson.optInt("workers", 2),
                                new Timestamp(System.currentTimeMillis()),
                                taskJson.optDouble("paymentWorker", 100.0),
                                taskJson.optInt("workers", 2) * taskJson.optDouble("paymentWorker", 100.0)
                        );

                        tasks.add(task);
                        taskService.create(task);
                    }

                    return tasks;
                } else {
                    System.err.println("No valid JSON array found in the response");
                    return null;
                }
            } else {
                System.err.println("Error from Gemini API: " + response.statusCode());
                System.err.println("Error response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception in generateFarmingTasks: ");
            e.printStackTrace();
            return null;
        }
    }
}