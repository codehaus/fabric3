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
