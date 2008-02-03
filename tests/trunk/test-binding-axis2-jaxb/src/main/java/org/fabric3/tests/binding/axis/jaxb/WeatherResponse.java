package org.fabric3.tests.binding.axis.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WeatherResponse {
    
    private WeatherCondition condition;
    private double temperatureMinimum;
    private double temperatureMaximum;
    
    public WeatherCondition getCondition() {
        return condition;
    }
    
    public void setCondition(WeatherCondition condition) {
        this.condition = condition;
    }
    
    public double getTemperatureMinimum() {
        return temperatureMinimum;
    }
    
    public void setTemperatureMinimum(double temperatureMinimum) {
        this.temperatureMinimum = temperatureMinimum;
    }
    
    public double getTemperatureMaximum() {
        return temperatureMaximum;
    }
    
    public void setTemperatureMaximum(double temperatureMaximum) {
        this.temperatureMaximum = temperatureMaximum;
    }

}
