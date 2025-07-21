import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import static io.restassured.RestAssured.given;

public class Weather {
    public static void main(String[] args) {
        new WeatherAppGUI().run();
    }
}

class WeatherApi {

    public WeatherResponse getApi(String city, int day) {
        return given()
                .queryParam("key", "718861da080d4c53b8f100742251007")
                .queryParam("q", city)
                .queryParam("days", day)
                .queryParam("lang", "ru")
                .queryParam("tp", 24)
                .get("https://api.weatherapi.com/v1/forecast.json")
                .then().extract().as(WeatherResponse.class);


    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class WeatherResponse {
        private Location location;
        private Current current;
        private Forecast forecast;

        @JsonIgnoreProperties(ignoreUnknown = true)

        @Data
        public static class Location {
            private String name;  // Название города (Moscow)
            private String localtime;  // Время и дата (2025-07-20 19:01)
        }

        @JsonIgnoreProperties(ignoreUnknown = true)

        @Data
        public static class Current {
            private String last_updated;  // Время последнего обновления (19:00)
            private Condition condition;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)

        @Data
        public static class Condition {
            private String text;  // Описание погоды (Небольшой ливневый дождь)
            private String icon;  // Ссылка на иконку
        }

        @JsonIgnoreProperties(ignoreUnknown = true)

        @Data
        public static class Forecast {
            private List<ForecastDay> forecastday;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)

        @Data
        public static class ForecastDay {
            private String date;  // Дата (2025-07-20)
            private Day day;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)

        @Data
        public static class Day {
            @JsonProperty("maxtemp_c")
            private double maxTempC;  // Максимальная температура (°C)

            @JsonProperty("mintemp_c")
            private double minTempC;  // Минимальная температура (°C)

            @JsonProperty("avgtemp_c")
            private double avgTempC;  // Средняя температура (°C)

            private Condition condition;  // Описание и иконка погоды
        }
    }
}

class WeatherAppGUI implements Runnable {

    private static void showWeatherForecast(String city, int days) {
        try {
            // Получаем данные через API
            WeatherApi.WeatherResponse response = new WeatherApi().getApi(city, days);
if (response.getLocation()==null||response.getCurrent()==null||response.getForecast()==null){
    throw new RuntimeException("Нет данных прогноза для выбранного города");
}
            // Создаем окно для отображения прогноза
            JFrame weatherFrame = new JFrame("Прогноз погоды для " + city);
            weatherFrame.setSize(600, 400);
            weatherFrame.setLayout(new BorderLayout());

            // Панель для прогноза
            JPanel weatherPanel = new JPanel();
            weatherPanel.setLayout(new BoxLayout(weatherPanel, BoxLayout.Y_AXIS));
            weatherPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Заголовок
            JLabel title = new JLabel("Прогноз погоды в " + city + " на " + days + " дней:");
            title.setFont(new Font("Arial", Font.BOLD, 16));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            weatherPanel.add(title);
            weatherPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Таблица с прогнозом
            String[] columnNames = {"Дата", "Мин (°C)", "Макс (°C)", "Средняя (°C)", "Погода"};
            Object[][] data = new Object[response.getForecast().getForecastday().size()][5];

            List<WeatherApi.WeatherResponse.ForecastDay> forecast = response.getForecast().getForecastday();
            for (int i = 0; i < forecast.size(); i++) {
                WeatherApi.WeatherResponse.ForecastDay day = forecast.get(i);
                data[i][0] = day.getDate();
                data[i][1] = day.getDay().getMinTempC();
                data[i][2] = day.getDay().getMaxTempC();
                data[i][3] = day.getDay().getAvgTempC();
                data[i][4] = day.getDay().getCondition().getText();
            }

            JTable weatherTable = new JTable(data, columnNames);
            weatherTable.setFillsViewportHeight(true);
            JScrollPane scrollPane = new JScrollPane(weatherTable);
            weatherPanel.add(scrollPane);

            // Текущая погода
            JPanel currentPanel = new JPanel(new GridLayout(1, 2));
            JLabel currentWeatherLabel = new JLabel("<html><b>Текущая погода:</b><br>" +
                    response.getCurrent().getCondition().getText() + "<br>" +
                    "Последнее обновление: " + response.getCurrent().getLast_updated());
            currentWeatherLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Иконка погоды
            String iconUrl = "https:" + response.getCurrent().getCondition().getIcon();
            ImageIcon weatherIcon = new ImageIcon(new URL(iconUrl));
            JLabel iconLabel = new JLabel(weatherIcon);

            currentPanel.add(currentWeatherLabel);
            currentPanel.add(iconLabel);
            weatherPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            weatherPanel.add(currentPanel);

            weatherFrame.add(weatherPanel, BorderLayout.CENTER);
            weatherFrame.setLocationRelativeTo(null);
            weatherFrame.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Ошибка при получении данных: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Создаем главное окно
        JFrame mainFrame = new JFrame("Погодное приложение");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 300);
        mainFrame.setLayout(new GridLayout(4, 2, 10, 10));

        // Добавляем отступы
        ((JComponent) mainFrame.getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Поле для ввода города
        JLabel cityLabel = new JLabel("Город:");
        JTextField cityField = new JTextField();
        mainFrame.add(cityLabel);
        mainFrame.add(cityField);

        // Поле для выбора дней прогноза
        JLabel daysLabel = new JLabel("Количество дней (1-14):");
        JComboBox<Integer> daysComboBox = new JComboBox<>();
        for (int i = 1; i <= 14; i++) {
            daysComboBox.addItem(i);
        }
        mainFrame.add(daysLabel);
        mainFrame.add(daysComboBox);

        // Кнопка для получения прогноза
        JButton getWeatherButton = new JButton("Получить прогноз");
        getWeatherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = cityField.getText();
                if (city.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Пожалуйста, введите город",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!isValidCityName(city)) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Название города должно содержать только буквы и пробелы",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int days = (int) daysComboBox.getSelectedItem();
                showWeatherForecast(city, days);
            }
        });

        mainFrame.add(new JLabel()); // Пустая ячейка для выравнивания
        mainFrame.add(getWeatherButton);

        // Центрируем окно и делаем видимым
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private static boolean isValidCityName(String input) {
        // Проверяем, что строка содержит только буквы (включая Unicode), пробелы и дефисы
        return input.matches("^[\\p{L} \\-']+$");
    }
}
