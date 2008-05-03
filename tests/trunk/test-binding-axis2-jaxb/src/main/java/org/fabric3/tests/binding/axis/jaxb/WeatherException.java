/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.tests.binding.axis.jaxb;

import javax.xml.ws.WebFault;

/**
 * @version $Rev$ $Date$
 */
@WebFault
public class WeatherException extends Exception {

    private static final long serialVersionUID = -5442856978625675327L;
    
    private BadWeatherFault faultInfo;

    public WeatherException(String message, BadWeatherFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public WeatherException(String message, BadWeatherFault faultInfo, Throwable throwable) {
        super(message, throwable);
        this.faultInfo = faultInfo;
    }

    public BadWeatherFault getFaultInfo() {
        return faultInfo;
    }
}
