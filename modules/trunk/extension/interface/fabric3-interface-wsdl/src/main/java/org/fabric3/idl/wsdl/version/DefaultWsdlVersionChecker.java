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
package org.fabric3.idl.wsdl.version;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Default implementation of the WSDL version checker.
 */
public class DefaultWsdlVersionChecker implements WsdlVersionChecker {

    /**
     * @see org.fabric3.idl.wsdl.version.WsdlVersionChecker#getVersion(java.net.URL)
     */
    public WsdlVersion getVersion(URL wsdlUrl) {

        InputStream wsdlStream = null;

        try {

            wsdlStream = wsdlUrl.openConnection().getInputStream();

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(wsdlStream);
            
            // Get to the document element
            while(reader.next() != START_ELEMENT) {
            }
            String localPart = reader.getName().getLocalPart();
            if ("definitions".equals(localPart)) {
                return WsdlVersion.VERSION_1_1;
            } else if ("description".equals(localPart)) {
                return WsdlVersion.VERSION_2_0;
            } else {
                throw new WsdlVersionCheckerException("Unknown document element " + localPart);
            }
            
        } catch (XMLStreamException ex) {
            throw new WsdlVersionCheckerException("Unable to read stream", ex);
        } catch (IOException ex) {
            throw new WsdlVersionCheckerException("Unable to read stream", ex);
        } finally {
            try {
                if (wsdlStream != null) {
                    wsdlStream.close();
                }
            } catch (IOException ignore) {
            }
        }

    }

}
