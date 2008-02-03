package org.fabric3.tests.binding.axis.jaxb;

public class WeatherServiceImpl implements WeatherService {

    public WeatherResponse getWeather(WeatherRequest weatherRequest) {
        
        System.err.println("Weather request received");
        
        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.setCondition(WeatherCondition.SUNNY);
        weatherResponse.setTemperatureMinimum(25);
        weatherResponse.setTemperatureMaximum(40);

        return weatherResponse;
        
    }


}
