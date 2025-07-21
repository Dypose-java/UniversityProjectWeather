import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class WeatherTest {
    @Test
    @DisplayName("Проверка корректного получения api погоды")
    void weatherApiPositiveTest() {
        String city = "Moscow";
        int day = 8;
        WeatherApi.WeatherResponse moscow = new WeatherApi().getApi(city, day);
        assertThat(moscow).isNotNull();
        assertThat(moscow.getLocation().getName()).isEqualTo(city);
        List<WeatherApi.WeatherResponse.ForecastDay> forecastday = moscow.getForecast().getForecastday();
        assertThat(forecastday.size()).isEqualTo(day);

    }

    @Test
    @DisplayName("Проверка некорректного города")
    void weatherApiNegativeCityTest() {
        String city = "Error_Message";
        int day = 8;
        WeatherApi.WeatherResponse moscow = new WeatherApi().getApi(city, day);
        assertThat(moscow.getLocation()).isNull();
        assertThat(moscow.getForecast()).isNull();
        assertThat(moscow.getCurrent()).isNull();
    }

    @Test
    @DisplayName("Проверка некорректной даты(поле day расчитано на value от 1 до 14)")
    void weatherApiNegativeDayTest() {
        String city = "Moscow";
        int day = 15;
        WeatherApi.WeatherResponse moscow = new WeatherApi().getApi(city, day);
        assertThat(moscow.getLocation()).isNull();
        assertThat(moscow.getForecast()).isNull();
        assertThat(moscow.getCurrent()).isNull();

    }

}
