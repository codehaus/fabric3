/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
