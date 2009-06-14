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

import java.io.IOException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.model.type.service.JavaServiceContract;
import org.fabric3.spi.generator.GenerationException;

public class DefaultWsdlElementParserTest extends TestCase {

    public void testWithWsdlElement() throws GenerationException {
        String element = "urn:weather#wsdl.port(WeatherService/WeatherPort)";
        WsdlElement wsdlElement = new DefaultWsdlElementParser().parseWsdlElement(element, null, null);
        assertEquals(new QName("urn:weather", "WeatherService"), wsdlElement.getServiceName());
        assertEquals(new QName("urn:weather", "WeatherPort"), wsdlElement.getPortName());
    }

    public void testWithServiceContract() throws GenerationException {
        JavaServiceContract javaServiceContract = new JavaServiceContract(TestService.class);
        WsdlElement wsdlElement = new DefaultWsdlElementParser().parseWsdlElement(null, null, javaServiceContract);        
        assertEquals(new QName("org.fabric3.binding.ws.metro.control", "TestServiceService"), wsdlElement.getServiceName());
        assertEquals(new QName("org.fabric3.binding.ws.metro.control", "TestServicePort"), wsdlElement.getPortName());
    }

    public void testWithWsdlLocation() throws GenerationException, IOException, XMLStreamException, SAXException {
        URL wsdlLocation = getClass().getClassLoader().getResource("Weather.wsdl");
        WSDLModel wsdlModel = new DefaultWsdlParser().parse(wsdlLocation);
        WsdlElement wsdlElement = new DefaultWsdlElementParser().parseWsdlElement(null, wsdlModel, null);
        assertEquals(new QName("urn:weather", "WeatherService"), wsdlElement.getServiceName());
        assertEquals(new QName("urn:weather", "WeatherPort"), wsdlElement.getPortName());
    }

}
