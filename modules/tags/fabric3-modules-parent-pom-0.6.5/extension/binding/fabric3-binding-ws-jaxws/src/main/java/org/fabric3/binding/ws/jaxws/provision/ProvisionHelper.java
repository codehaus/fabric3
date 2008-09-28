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
package org.fabric3.binding.ws.jaxws.provision;

public final class ProvisionHelper {

    private static final String WSDL_11 = "#wsdl.port(";
    private static final int WSDL_11_LENGTH = WSDL_11.length();


    private static int assertWSDL11(String wsdlElement) {
        int index = wsdlElement.indexOf(WSDL_11);
        if (index < 0) {
            AssertionError ae = new AssertionError("Only WSDL 1.1 are supported");
            throw ae;
        }
        return index;
    }

    public static String[] parseWSDLElement(String wsdlElement) {
        int index = assertWSDL11(wsdlElement);
        String[] parsed = new String[3];
        parsed[0] = wsdlElement.substring(0, index);
        String postURI = wsdlElement.substring(index + WSDL_11_LENGTH);
        index = postURI.indexOf('/');
        parsed[1] = postURI.substring(0, index);
        parsed[2] = postURI.substring(index + 1, postURI.lastIndexOf(")"));
        return parsed;
    }


}
