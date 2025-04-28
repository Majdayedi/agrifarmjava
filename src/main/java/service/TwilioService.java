package service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.util.Arrays;
import java.util.List;
import java.io.InputStream;
import java.util.Properties;

public class TwilioService {
    private static String TWILIO_PHONE_NUMBER;

    // List of severe weather conditions that should trigger alerts
    private static final List<String> SEVERE_WEATHER_CONDITIONS = Arrays.asList(
            "light rain", "thunderstorm", "hail", "extreme heat", "frost",
            "strong winds", "blizzard", "tornado", "dense fog", "extreme cold"
    );

    static {
        try {
            // Load configuration from properties file
            Properties props = new Properties();
            InputStream input = TwilioService.class.getClassLoader().getResourceAsStream("config.properties");
            if (input != null) {
                props.load(input);
                String id = props.getProperty("twilio.account.sid");
                String tok = props.getProperty("twilio.auth.token");
                TWILIO_PHONE_NUMBER = props.getProperty("twilio.phone.number");

                Twilio.init(id, tok);
            } else {
                System.err.println("Unable to find config.properties");
            }
        } catch (Exception e) {
            System.err.println("Error loading Twilio configuration: " + e.getMessage());
        }
    }

    /**
     * Check if the weather condition is severe and send an alert if necessary
     * @param phoneNumber The recipient's phone number
     * @param farmName The name of the farm
     * @param weatherDescription The current weather description
     * @param temperature The current temperature
     */
    public static void checkAndSendWeatherAlert(String phoneNumber, String farmName, String weatherDescription, double temperature) {
        // Convert weather description to lowercase for case-insensitive comparison
        String normalizedDescription = weatherDescription.toLowerCase();

        // Check if the weather condition is in our list of severe conditions
        if (SEVERE_WEATHER_CONDITIONS.stream().anyMatch(condition ->
                normalizedDescription.contains(condition.toLowerCase()))) {

            // Send the alert
            sendWeatherAlert(phoneNumber, farmName, weatherDescription, temperature);
        }
    }

    /**
     * Send an SMS message using Twilio
     * @param toPhoneNumber The recipient's phone number
     * @param message The message content
     */
    public static void sendSMS(String toPhoneNumber, String message) {
        try {
            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    message
            ).create();
            System.out.println("SMS sent successfully to " + toPhoneNumber);
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
        }
    }

    /**
     * Send a weather alert SMS
     * @param toPhoneNumber The recipient's phone number
     * @param farmName The name of the farm
     * @param weatherDescription The weather description
     * @param temperature The temperature
     */
    public static void sendWeatherAlert(String toPhoneNumber, String farmName, String weatherDescription, double temperature) {
        String message = String.format(
                "Weather Alert for %s:\nCurrent Conditions: %s\nTemperature: %.1f°C",
                farmName,
                weatherDescription,
                temperature
        );
        sendSMS(toPhoneNumber, message);
    }
} 