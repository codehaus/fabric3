package org.fabric3.tests.binding.axis.jaxb;

import java.util.Date;
import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

public class WeatherServiceTest extends TestCase {
    
    private WeatherService weatherService;

    @Reference
    public void setWeatherService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public void testGetWeather() {
        
        WeatherRequest weatherRequest = new WeatherRequest();
        weatherRequest.setCity("London");
        weatherRequest.setDate(new Date());
        
        WeatherResponse weatherResponse = weatherService.getWeather(weatherRequest);
        
        assertEquals(WeatherCondition.SUNNY, weatherResponse.getCondition());
        assertEquals(25.0, weatherResponse.getTemperatureMinimum());
        assertEquals(40.0, weatherResponse.getTemperatureMaximum());
        
    }

    public void testBadWeather() {
        try {
            weatherService.getBadWeather();
            fail();
        } catch (WeatherException e) {
            // expected
        }
    }


}
