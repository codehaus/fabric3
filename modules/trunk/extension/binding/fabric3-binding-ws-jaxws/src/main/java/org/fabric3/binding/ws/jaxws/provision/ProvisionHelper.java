package org.fabric3.binding.ws.jaxws.provision;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
