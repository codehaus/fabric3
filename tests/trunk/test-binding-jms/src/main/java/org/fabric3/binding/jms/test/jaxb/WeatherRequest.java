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
package org.fabric3.binding.jms.test.jaxb;

import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * @version $Revision$ $Date$
 */
@XmlRootElement
public class WeatherRequest implements Serializable {
    private static final long serialVersionUID = -3896071380449163733L;

    private String city;

    private Date date;

    @XmlElement
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    @XmlElement
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

}