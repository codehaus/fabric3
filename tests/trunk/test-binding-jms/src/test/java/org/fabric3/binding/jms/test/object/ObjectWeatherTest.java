/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.binding.jms.test.object;

import java.util.Date;

import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

import org.fabric3.binding.jms.test.object.WeatherCondition;
import org.fabric3.binding.jms.test.object.WeatherRequest;
import org.fabric3.binding.jms.test.object.WeatherResponse;
import org.fabric3.binding.jms.test.object.WeatherService;

/**
 * @version $Revision$ $Date$
 */
public class ObjectWeatherTest extends TestCase {
    
    private WeatherService weatherService;
    
    @Reference
    public void setWeatherService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    public void testWeather() {
        
        WeatherRequest weatherRequest = new WeatherRequest();
        weatherRequest.setCity("London");
        weatherRequest.setDate(new Date());
        
        WeatherResponse weatherResponse = weatherService.getWeather(weatherRequest);
        
        assertEquals(WeatherCondition.SUNNY, weatherResponse.getCondition());
        assertEquals(25.0, weatherResponse.getTemperatureMinimum());
        assertEquals(40.0, weatherResponse.getTemperatureMaximum());
        
    }

}
