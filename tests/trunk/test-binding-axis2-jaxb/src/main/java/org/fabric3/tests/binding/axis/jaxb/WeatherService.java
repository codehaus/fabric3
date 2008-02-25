package org.fabric3.tests.binding.axis.jaxb;

public interface WeatherService {
    
    WeatherResponse getWeather(WeatherRequest request);

    void getBadWeather() throws WeatherException;

}
