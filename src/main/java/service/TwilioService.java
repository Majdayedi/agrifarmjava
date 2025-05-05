package service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.util.Arrays;
import java.util.List;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwilioService {
    private static final Logger LOGGER = Logger.getLogger(TwilioService.class.getName());
    private static String ACCOUNT_SID;
    private static String AUTH_TOKEN;
    private static String PHONE_NUMBER;
    private static boolean isInitialized = false;

    // List of severe weather conditions that should trigger alerts
    private static final List<String> SEVERE_WEATHER_CONDITIONS = Arrays.asList(
            "light rain", "thunderstorm", "hail", "extreme heat", "frost",
            "strong winds", "blizzard", "tornado", "dense fog", "extreme cold"
    );

    private static void initializeIfNeeded() {
        if (!isInitialized) {
            try {
                // Load configuration from properties file
                Properties props = new Properties();
                InputStream input = TwilioService.class.getClassLoader().getResourceAsStream("config.properties");
                if (input != null) {
                    props.load(input);

                    // Get credentials from configuration
                    ACCOUNT_SID = props.getProperty("twilio.account.sid");
                    AUTH_TOKEN = props.getProperty("twilio.auth.token");
                    PHONE_NUMBER = props.getProperty("twilio.phone.number");

                    if (ACCOUNT_SID == null || AUTH_TOKEN == null || PHONE_NUMBER == null) {
                        LOGGER.severe("Twilio configuration is incomplete in config.properties");
                        return;
                    }

                    // Initialize Twilio with credentials
                    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                    isInitialized = true;
                    
                } else {
                    LOGGER.severe("Unable to find config.properties");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading Twilio configuration: " + e.getMessage(), e);
            }
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
            initializeIfNeeded();
            if (!isInitialized) {
                LOGGER.severe("Cannot send SMS: Twilio is not properly initialized");
                return;
            }

            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(PHONE_NUMBER),
                    message
            ).create();
            LOGGER.info("SMS sent successfully to " + toPhoneNumber);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending SMS: " + e.getMessage(), e);
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