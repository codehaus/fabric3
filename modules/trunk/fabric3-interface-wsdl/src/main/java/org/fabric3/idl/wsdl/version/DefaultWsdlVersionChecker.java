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
            while(true) {
                switch(reader.next()) {
                    case START_ELEMENT:
                        String localPart = reader.getName().getLocalPart();
                        if("definitions".equals(localPart)) {
                            return WsdlVersion.VERSION_1_1;
                        } else if("description".equals(localPart)) {
                            return WsdlVersion.VERSION_2_0;
                        }
                }
                throw new WsdlVersionCheckerException("Unable to determine WSDL version");
            }
        } catch(XMLStreamException ex) {
            throw new WsdlVersionCheckerException("Unable to read stream", ex);
        } catch(IOException ex) {
            throw new WsdlVersionCheckerException("Unable to read stream", ex);
		} finally {
			try {
				if(wsdlStream != null) {
					wsdlStream.close();
				}
			} catch(IOException ignore) {
            }
		}

    }

}
