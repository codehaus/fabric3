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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.util.stax.StaxUtil;
import org.osoa.sca.annotations.Reference;

/**
 * Loader for definitions.
 * 
 * @version $Revision$ $Date$
 */
public class IntentLoader implements StAXElementLoader<Intent> {

    /**
     * Registers the loader with the registry.
     * @param registry Injected registry
     */
    public IntentLoader(@Reference LoaderRegistry registry) {
        registry.registerLoader(DefinitionsLoader.INTENT, this);
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public Intent load(XMLStreamReader reader, LoaderContext context) throws XMLStreamException, LoaderException {
        
        String name = reader.getAttributeValue(null, "name");
        QName qName = new QName(context.getTargetNamespace(), name);
        
        Set<QName> constrains = new HashSet<QName>();
        StringTokenizer tok = new StringTokenizer(reader.getAttributeValue(null, "constrains"));
        while(tok.hasMoreElements()) {
            constrains.add(StaxUtil.createQName(tok.nextToken(), reader));
        }
        
        String description = null;
        
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                if (DefinitionsLoader.DESCRIPTION.equals(reader.getName())) {
                    description = reader.getElementText();
                }
                break;
            case END_ELEMENT:
                if (DefinitionsLoader.INTENT.equals(reader.getName())) {
                    return new Intent(qName, description, constrains);
                }
            }
        }
        
    }

}
