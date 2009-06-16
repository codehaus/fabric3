/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.ws.metro.provision;

import java.io.Serializable;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 * Encapsulates endpoint information for the reference side of an invocation chain.
 *
 * @version $Rev$ $Date$
 */
public class ReferenceEndpointDefinition implements Serializable {
    private static final long serialVersionUID = -7422624061436929193L;
    private QName serviceName;
    private QName portName;
    private URL url;

    /**
     * Constructor.
     *
     * @param serviceName the qualified name of the target service
     * @param portName    the qualified name of the target port
     * @param url         the endpoint URL
     */
    public ReferenceEndpointDefinition(QName serviceName, QName portName, URL url) {
        this.serviceName = serviceName;
        this.portName = portName;
        this.url = url;
    }

    /**
     * Returns the qualified service name.
     *
     * @return the qualified service name
     */
    public QName getServiceName() {
        return serviceName;
    }

    /**
     * Returns the qualified port name.
     *
     * @return the qualified port name
     */
    public QName getPortName() {
        return portName;
    }

    /**
     * Returns the endpoint URL.
     *
     * @return the endpoint URL
     */
    public URL getUrl() {
        return url;
    }

}
