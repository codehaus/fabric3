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
package org.fabric3.loader.definitions;

import java.io.InputStream;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;

/**
 * @version $Revision$ $Date$
 */
public class DefinitionsIndexerTestCase extends TestCase {
    DefinitionsIndexer loader;
    private XMLStreamReader reader;

    public void testIndex() throws Exception {
        Resource resource = new Resource(null, "foo");
        loader.index(resource, reader);

        List<ResourceElement<?, ?>> resourceElements = resource.getResourceElements();
        assertNotNull(resourceElements);
        assertEquals(2, resourceElements.size());

        ResourceElement<?, ?> intentResourceElement = resourceElements.get(0);
        QNameSymbol symbol = (QNameSymbol) intentResourceElement.getSymbol();
        assertEquals(new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactional"), symbol.getKey());

        ResourceElement<?, ?> policySetResourceElement = resourceElements.get(1);
        symbol = (QNameSymbol) policySetResourceElement.getSymbol();
        assertEquals(new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactionalPolicy"), symbol.getKey());
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new DefinitionsIndexer(null);
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }
}