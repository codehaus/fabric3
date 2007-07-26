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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.loader.definitions.DefinitionsLoader;
import org.fabric3.loader.definitions.IntentLoader;
import org.fabric3.loader.definitions.PolicySetLoader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.scdl.definitions.Definitions;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.osoa.sca.Constants;

/**
 * @version $Revision$ $Date$
 */
public class DefinitionsLoaderTestCase extends TestCase {

    public void testLoad() throws Exception {
        
        ClassLoader cl = null;
        LoaderContext context = new LoaderContextImpl(cl, null);
        
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        LoaderRegistry loaderRegistry = new LoaderRegistryImpl();
        
        DefinitionsLoader loader = new DefinitionsLoader(loaderRegistry);
        IntentLoader intentLoader = new IntentLoader(loaderRegistry);
        PolicySetLoader policySetLoader = new PolicySetLoader(loaderRegistry);
        
        while(reader.next() != XMLStreamConstants.START_ELEMENT) {
        }
        
        Definitions definitions = loader.load(reader, context);
        
        assertEquals(1, definitions.getIntents().size());
        Intent intent = definitions.getIntents().iterator().next();
        assertEquals(new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactional"), intent.getName());
        assertTrue(intent.doesConstrain(new QName(Constants.SCA_NS, "service")));
        assertFalse(intent.doesConstrain(new QName(Constants.SCA_NS, "reference")));
        
        assertEquals(1, definitions.getPolicySets().size());
        PolicySet policySet = definitions.getPolicySets().iterator().next();
        assertTrue(policySet.doesProvide(new QName("http://fabric3.org/xmlns/sca/2.0-alpha", "transactional")));
    }
    
    private static class LoaderRegistryImpl implements LoaderRegistry {
        
        private Map<QName, StAXElementLoader<?>> loaders = new HashMap<QName, StAXElementLoader<?>>();

        public void registerLoader(QName element, StAXElementLoader<?> loader)
                throws IllegalStateException {
            loaders.put(element, loader);
        }

        public void unregisterLoader(QName element) {
        }

        public <OUTPUT> OUTPUT load(XMLStreamReader reader, Class<OUTPUT> type,
                LoaderContext context) throws XMLStreamException,
                LoaderException {
            return (OUTPUT) loaders.get(reader.getName()).load(reader, context);
        }

        public <OUTPUT> OUTPUT load(URL url, Class<OUTPUT> type,
                LoaderContext context) throws LoaderException {
            // TODO Auto-generated method stub
            return null;
        }
        
    }

}
