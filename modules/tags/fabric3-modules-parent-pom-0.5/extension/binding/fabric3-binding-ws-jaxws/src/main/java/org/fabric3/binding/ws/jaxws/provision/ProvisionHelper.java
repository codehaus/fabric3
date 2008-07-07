package org.fabric3.binding.ws.jaxws.provision;

/**
 * @author Copyright (c) 2008 by BEA Systems. All Rights Reserved.
 */
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
