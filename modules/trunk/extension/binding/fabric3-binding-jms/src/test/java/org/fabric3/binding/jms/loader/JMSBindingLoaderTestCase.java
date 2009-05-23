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
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.w3c.dom.Document;

import org.fabric3.binding.jms.common.HeadersDefinition;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.loader.JmsBindingLoader;
import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.LoaderHelper;

public class JMSBindingLoaderTestCase extends TestCase {
    public void testLoaderJMSBindingElement() throws Exception {
        LoaderHelper loaderHelper = new LoaderHelper() {

			public QName createQName(String name, XMLStreamReader reader)
					throws InvalidPrefixException {
				// TODO Auto-generated method stub
				return null;
			}

			public URI getURI(String target) {
				// TODO Auto-generated method stub
				return null;
			}

			public Document loadKey(XMLStreamReader reader) {
				// TODO Auto-generated method stub
				return null;
			}

			public void loadPolicySetsAndIntents(PolicyAware policyAware,
					XMLStreamReader reader, IntrospectionContext context) {
				// TODO Auto-generated method stub
				
			}

			public Document loadValue(XMLStreamReader reader)
					throws XMLStreamException {
				// TODO Auto-generated method stub
				return null;
			}

			public Set<QName> parseListOfQNames(XMLStreamReader reader,
					String attribute) throws InvalidPrefixException {
				// TODO Auto-generated method stub
				return null;
			}

			public List<URI> parseListOfUris(XMLStreamReader reader,
					String attribute) {
				// TODO Auto-generated method stub
				return null;
			}
        	
        };
        JmsBindingLoader loader = new JmsBindingLoader(loaderHelper);
        InputStream inputStream = JmsBindingLoader.class
                .getResourceAsStream("JMSBindingLoaderTest.xml");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader streamReader = factory
                .createXMLStreamReader(new InputStreamReader(inputStream));
        loaderHelper.loadKey(streamReader);
        JmsBindingDefinition jmsBinding = null;
        while (streamReader.hasNext()) {
            if (START_ELEMENT == streamReader.next()
                    && "binding.jms".equals(streamReader.getName()
                    .getLocalPart())) {
                jmsBinding = loader.load(streamReader, null);
                streamReader.close();
                break;
            }
        }
        JmsBindingMetadata metadata = jmsBinding.getMetadata();
        HeadersDefinition headers = metadata.getHeaders();
        assertEquals("CorrelationId", headers.getJMSCorrelationId());
        assertEquals("ThisType", headers.getJMSType());
        assertEquals("TestHeadersProperty", headers.getProperties().get(
                "testHeadersProperty"));
        assertEquals(2, metadata.getOperationProperties().size());
        assertEquals("TestHeadersPropertyProperty", metadata
                .getOperationProperties().get("testOperationProperties1")
                .getProperties().get("testHeadersPropertyProperty"));
        assertEquals("NestedHeader", metadata
                .getOperationProperties().get("testOperationProperties1").getHeaders()
                .getProperties().get("nested"));
        assertEquals("NativeName", metadata.getOperationProperties().get(
                "testOperationProperties2").getNativeOperation());
    }

}
