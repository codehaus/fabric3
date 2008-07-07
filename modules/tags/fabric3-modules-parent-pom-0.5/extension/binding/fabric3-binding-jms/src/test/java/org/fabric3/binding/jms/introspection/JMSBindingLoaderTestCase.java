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
package org.fabric3.binding.jms.introspection;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.fabric3.binding.jms.common.HeadersDefinition;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.scdl.JmsBindingDefinition;
import org.fabric3.introspection.xml.LoaderHelper;

public class JMSBindingLoaderTestCase extends TestCase {
    public void testLoaderJMSBindingElement() throws Exception {
        LoaderHelper loaderHelper = EasyMock.createMock(LoaderHelper.class);
        JmsBindingLoader loader = new JmsBindingLoader(loaderHelper);
        InputStream inputStream = JmsBindingLoader.class
                .getResourceAsStream("JMSBindingLoaderTest.xml");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader streamReader = factory
                .createXMLStreamReader(new InputStreamReader(inputStream));
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
