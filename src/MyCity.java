import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class MyCity {

    private static final double KELVIN_OFFSET = 273.15;
    private static final double METER_TO_FEET_MULTIPLIER = 3.280839895;
    private static final int RETRY_COUNT = 3;

    /**
     * Fetch JSON object from the provided URL and return it as JsonElement.
     *
     * @param urlStr URL to visit
     * @return JsonElement if response was a valid JSON object, null otherwise
     */
    private static JsonElement getJsonFromUrl(String urlStr) {
        HttpURLConnection conn;
        try {
            int respCode;
            int attempt = 0;
            do {
                TimeUnit.SECONDS.sleep(attempt);
                conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                respCode = conn.getResponseCode();
                attempt++;
            } while (500 <= respCode && respCode < 600 && attempt <= RETRY_COUNT);
            Reader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return JsonParser.parseReader(reader);
        } catch (IOException | InterruptedException e) {
            System.out.println("Invalid URL");
            return null;
        }
    }


    public static void main(String[] args) {
        String syntax = "java -jar MyCity.jar [options] <city_name> <api_key>";

        Options options = new Options();

        // Optional parameter for the state code
        Option option = new Option("s", "state-code", true, "state code (Only available for the US cities)");
        option.setArgName("code");
        options.addOption(option);

        // Optional parameter for the country code
        option = new Option("c", "country-code", true, "country code (see ISO 3166)");
        option.setArgName("code");
        options.addOption(option);

        options.addOption(new Option("h", "help", false, "print this message"));


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Incorrect parameters");
            formatter.printHelp(syntax, options);
            System.exit(1);
        }

        // Help overrides existing parameters and exits the program after printing out usage information
        if (cmd.hasOption("h")) {
            formatter.printHelp(syntax, options);
            return;
        }

        // Users are expected to escape the input parameter
        if (cmd.getArgs().length != 2) {
            System.out.println("You must pass in exactly one city name and an API key");
            System.out.println("To pass in a city name with a space, use quotation marks: e.g. \"San Francisco\"");
            formatter.printHelp(syntax, options);
            return;
        }

        String stateCode = cmd.getOptionValue("s");
        String countryCode = cmd.getOptionValue("c");
        String cityName = cmd.getArgs()[0];
        String api_key = cmd.getArgs()[1];

        if (stateCode == null) {
            stateCode = "";
        }
        if (countryCode == null) {
            countryCode = "";
        }

        JsonElement jsonResponse = MyCity.getJsonFromUrl("https://api.openweathermap.org/geo/1.0/direct?q=" +
                cityName + "," + stateCode + "," + countryCode + "&limit=5&appid=" + api_key);
        if (jsonResponse == null) {
            System.out.println("Location API is currently not functional, please verify your parameters and try again later.");
            return;
        }

        JsonArray resultArr = jsonResponse.getAsJsonArray();
        if (resultArr.size() == 0) {
            System.out.println("No city found with the provided name");
            return;
        }

        JsonObject curr;
        String lat, lon;
        double temp;
        for (int i = 0; i < resultArr.size(); i++) {
            curr = resultArr.get(i).getAsJsonObject();
            cityName = curr.get("name").getAsString();
            System.out.println("City name:\t" + cityName);
            // State may or may not exist
            if (curr.get("state") != null) {
                System.out.println("State name:\t" + curr.get("state").getAsString());
            }
            System.out.println("Country code:\t" + curr.get("country").getAsString());
            lat = curr.get("lat").getAsString();
            lon = curr.get("lon").getAsString();

            // Fetch weather data
            jsonResponse = MyCity.getJsonFromUrl("https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                    "&lon=" + lon + "&appid=" + api_key);
            if (jsonResponse == null) {
                System.out.println("Weather information unavailable");
            } else {
                temp = jsonResponse.getAsJsonObject()
                        .getAsJsonObject("main")
                        .get("temp")
                        .getAsDouble() - KELVIN_OFFSET;
                System.out.printf("Temperature:\t%.2f°C / %.2f°F%n", temp, temp * 1.8 + 32);
                System.out.printf("Weather: \t%s%n", jsonResponse.getAsJsonObject()
                        .get("weather").getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .get("description").getAsString());
            }

            // Fetch elevation data
            jsonResponse = MyCity.getJsonFromUrl("https://api.opentopodata.org/v1/aster30m?locations=" + lat + "," + lon);
            if (jsonResponse == null) {
                System.out.println("Elevation data unavailable");
            } else {
                temp = jsonResponse.getAsJsonObject()
                        .get("results").getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .get("elevation").getAsDouble();
                System.out.printf("Elevation:\t%.0fm / %.2fft%n", temp, temp * METER_TO_FEET_MULTIPLIER);
            }

            if (0 < resultArr.size() && i < resultArr.size() - 1) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
                System.out.println();
            }
        }
    }
}
