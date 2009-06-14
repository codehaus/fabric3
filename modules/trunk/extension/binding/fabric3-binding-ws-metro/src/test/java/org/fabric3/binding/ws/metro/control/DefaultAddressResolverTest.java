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
package org.fabric3.binding.ws.metro.control;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import junit.framework.TestCase;

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.spi.generator.GenerationException;

public class DefaultAddressResolverTest extends TestCase {
    
    private AddressResolver addressResolver = new DefaultAddressResolver();
    private WsdlParser wsdlParser = new DefaultWsdlParser();

    public void testResolveServiceAddressWithTargetUriSuccess() throws GenerationException {
        URI targetUri = URI.create("/weather");
        assertEquals(targetUri, addressResolver.resolveServiceAddress(targetUri, null, null));
    }

    public void testResolveReferenceAddressWithTargetUriSuccess() throws GenerationException, MalformedURLException {
        URI targetUri = URI.create("http://localhost:7001/weather,http://localhost:7002/weather");
        URL[] urls = addressResolver.resolveReferenceAddress(targetUri, null, null);
        assertEquals(2, urls.length);
        assertEquals(new URL("http://localhost:7001/weather"), urls[0]);
        assertEquals(new URL("http://localhost:7002/weather"), urls[1]);
    }

    public void testResolveServiceAddressWithWsdlElementSuccess() throws GenerationException {
        WsdlElement wsdlElement = new WsdlElement(new QName("urn:weather", "WeatherService"), new QName("urn:weather", "WeatherPort"));
        WSDLModel wsdlModel = wsdlParser.parse(getClass().getClassLoader().getResource("WeatherServerSide.wsdl"));
        assertEquals( URI.create("/weather"), addressResolver.resolveServiceAddress(null, wsdlElement, wsdlModel));
    }

    public void testResolveReferenceAddressWithWsdlElementSuccess() throws GenerationException, MalformedURLException {
        WsdlElement wsdlElement = new WsdlElement(new QName("urn:weather", "WeatherService"), new QName("urn:weather", "WeatherPort"));
        WSDLModel wsdlModel = wsdlParser.parse(getClass().getClassLoader().getResource("WeatherClientSide.wsdl"));
        URL[] urls = addressResolver.resolveReferenceAddress(null, wsdlElement, wsdlModel);
        assertEquals(2, urls.length);
        assertEquals(new URL("http://localhost:7001/weather"), urls[0]);
        assertEquals(new URL("http://localhost:7002/weather"), urls[1]);
    }

}
