
package weatherapp;

//Scanner class
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//Apache client server components
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

//The class to get data from .env files
import io.github.cdimascio.dotenv.Dotenv;

import org.json.JSONObject;
import org.json.JSONArray;

public class WeatherApp {
    public static void main(String[] args) {

        // Get the API Key from a local .env file
        Dotenv dotenv = Dotenv.configure().load();
        API_KEY = dotenv.get("API_KEY");
        DISCORD_KEY = dotenv.get("DISCORD_KEY");
        USER_ID = dotenv.get("USER_ID");
        LOCATION_DATA = dotenv.get("LOCATION_DATA");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String path = String.format("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/%s/%s?key=%s&include=current", LOCATION_DATA, "today", API_KEY);
            HttpGet httpGet = new HttpGet(path);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                if (statusCode == 400) {
                    System.out.print("Invalid Request! Please try again!!");
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    String weatherString = processOutput(responseBody);
                    discordPost(weatherString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String processOutput(String responseString) {
        JSONObject jsonResponse = new JSONObject(responseString);
        JSONArray days = jsonResponse.getJSONArray("days");

        StringBuffer returnString = new StringBuffer();


        Iterator daysIter = days.iterator();

        while (daysIter.hasNext()) {
            JSONObject currentDay = (JSONObject) daysIter.next();
            returnString.append(String.format("Here is the weather report for %s on %s\\n\\n", jsonResponse.get("resolvedAddress"), currentDay.get("datetime")));
            returnString.append(String.format("General Description: %s\\n\\n", currentDay.get("description")));
            returnString.append(String.format("The current temperature is %sF, but it feels like %sF.\\n", currentDay.get("temp"), currentDay.get("feelslike")));
            returnString.append(String.format("The maximum temperature is %sF and it will feel like %sF.\\n", currentDay.get("tempmax"), currentDay.get("feelslikemax")));
            returnString.append(String.format("The minimum temperature is %sF and it will feel like %sF.\\n", currentDay.get("tempmin"), currentDay.get("feelslikemin")));
            returnString.append("\\n");


            returnString.append(String.format("Today there is a %s%% chance that some type of precipitation will occur today.\\n", currentDay.get("precipprob")));

            boolean willSnow = false;
            if (!currentDay.get("preciptype").toString().equals("null")) {
                int index = 0;
                returnString.append("It will ");
                JSONArray precipTypes = (JSONArray) currentDay.get("preciptype");
                Iterator precipIter = precipTypes.iterator();
                while (precipIter.hasNext()) {
                    String precipString = (String) precipIter.next();

                    if (precipString.equals("snow")) {
                        willSnow = true;
                    }

                    if (!precipIter.hasNext()) {
                        if (index == 0) {
                            returnString.append(String.format("%s.", precipString));
                        } else if (index == 1) {
                            returnString.append(String.format(" or %s.", precipString));
                        } else {
                            returnString.append(String.format(", or %s.", precipString));
                        }
                    } else {
                        if (index == 0) {
                            returnString.append(String.format("%s", precipString));
                        } else {
                            returnString.append(String.format(", %s", precipString));
                        }
                    }

                    index++;
                }
                returnString.append("\\n");
            }

            if (willSnow) {
                returnString.append(String.format("There is a chance it will snow today!! There will be %s inches of snow.\\n", currentDay.get("snow")));
            }

            returnString.append("\\n");

            returnString.append(String.format("Today's maximum windspeed is %s mph\\n\\n", currentDay.get("windspeed")));
            returnString.append(String.format("Today the sun will set at %s\\n", currentDay.get("sunset")));
       }

       return returnString.toString();
    }



    public static void discordPost(String postString) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost getDMChannel = new HttpPost(String.format("https://discord.com/api/v10/users/@me/channels"));
            getDMChannel.setHeader(HttpHeaders.AUTHORIZATION, "Bot " + DISCORD_KEY);
            getDMChannel.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            StringEntity recipients = new StringEntity(String.format("{\"recipient_id\":\"%s\"}", USER_ID));
            getDMChannel.setEntity(recipients);

            try (CloseableHttpResponse response = httpClient.execute(getDMChannel)) {
                int statusCode = response.getCode();
                if (statusCode == 400) {
                    System.out.print("Invalid Request! Please try again!!");
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    CHANNEL_ID = (String) jsonResponse.get("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpPost sendWeatherMessage = new HttpPost(String.format("https://discord.com/api/v10/channels/%s/messages", CHANNEL_ID));  
            sendWeatherMessage.setHeader(HttpHeaders.AUTHORIZATION, "Bot " + DISCORD_KEY);
            sendWeatherMessage.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            StringEntity messageEntity = new StringEntity(String.format("{\"content\":\"%s\"}", postString));
            sendWeatherMessage.setEntity(messageEntity);
            try (CloseableHttpResponse response = httpClient.execute(sendWeatherMessage)) {
                int statusCode = response.getCode();
                if (statusCode == 400) {
                    System.out.println("Communication error!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    private static String API_KEY;
    private static String DISCORD_KEY;
    private static String CHANNEL_ID;
    private static String USER_ID;
    private static String LOCATION_DATA;

}
