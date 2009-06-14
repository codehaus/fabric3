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
import java.io.InputStream;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.spi.generator.GenerationException;

/*
 * Default implementation of the WSDL parser.
 */
public class DefaultWsdlParser implements WsdlParser {

    /**
     * Parses a WSDL document to the information model.
     *
     * @param wsdlLocation Location of the WSDL document.
     * @return WSDL model object.
     * @throws GenerationException If unable to parse the WSDL.
     */
    public WSDLModel parse(URL wsdlLocation) throws GenerationException {

        if (wsdlLocation == null) {
            return null;
        }

        InputStream inputStream = null;
        try {
            inputStream = wsdlLocation.openStream();
            return RuntimeWSDLParser.parse(wsdlLocation, new StreamSource(inputStream), entityResolver, false, null);
        } catch (XMLStreamException e) {
            throw new GenerationException(e);
        } catch (IOException e) {
            throw new GenerationException(e);
        } catch (SAXException e) {
            throw new GenerationException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

    }

    /*
    * Entity resolution is not currently supported.
    */
    private EntityResolver entityResolver = new EntityResolver() {
        public InputSource resolveEntity(String systemId, String publicId) throws SAXException, IOException {
            return null;
        }
    };

}
