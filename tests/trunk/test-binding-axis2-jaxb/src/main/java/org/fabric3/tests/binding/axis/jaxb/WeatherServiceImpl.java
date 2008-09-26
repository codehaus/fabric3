/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
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

    public void getBadWeather() throws WeatherException {
        BadWeatherFault fault = new BadWeatherFault();
        fault.setType("Bad weather today");
        throw new WeatherException(null, fault);
    }
}
