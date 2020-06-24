package by.example.bot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WeatherBot extends TelegramLongPollingBot {
    private static final String API_KEY = "f02c5f07c8d826c5de303e4b6d365768";
    private static String location = "Minsk";
    private static String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" +
            location + "&appid=" + API_KEY;
    private static final double KELVIN_TO_CELSIUS = 273.15;

    public static Map<String, Object> jsonToMap(String line) {
        return new Gson().fromJson(line, new TypeToken<HashMap<String, Object>>() {
        }.getType());
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            SendMessage sendMessage = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(update.getMessage().getText());
            if (text.equals("weather in Minsk")) {
                try {
                    sendMessage.setText(getWeather());
                } catch (IOException e) {
                    sendMessage.setText("Error");
                } catch (RuntimeException e) {
                    sendMessage.setText(e.getMessage());
                }
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String getWeather() throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        StringBuilder result = new StringBuilder();
        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                result.append(scanner.nextLine());
            }
            scanner.close();
        }
        Map<String, Object> responseMap = jsonToMap(result.toString());
        Map<String, Object> mainMap = jsonToMap(responseMap.get("main").toString());
        double tempInKelvin = Double.parseDouble(mainMap.get("temp").toString());
        return (tempInKelvin - KELVIN_TO_CELSIUS) + " °C";
    }

    @Override
    public String getBotUsername() {
        return "weatherBot";
    }

    @Override
    public String getBotToken() {
        return "901513356:AAH6vG1B3S0hKfrAeIGumUXCZT2BFX4Cg-E";
    }
}
