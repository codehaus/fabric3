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
package org.fabric3.loader.definition;

import static org.osoa.sca.Constants.SCA_NS;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.model.definition.Definitions;
import org.osoa.sca.annotations.Reference;

/**
 * Loader for definitions.
 * 
 * @version $Revision$ $Date$
 */
public class DefinitionsLoader implements StAXElementLoader<Definitions> {
    
    private static final QName DEFINITIONS = new QName(SCA_NS, "definitions");
    
    private LoaderRegistry loaderRegistry;

    public DefinitionsLoader(@Reference LoaderRegistry registry) {
        this.loaderRegistry = loaderRegistry;
        loaderRegistry.registerLoader(DEFINITIONS, this);
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public Definitions load(XMLStreamReader reader, LoaderContext context) throws XMLStreamException, LoaderException {
        // TODO Auto-generated method stub
        return null;
    }

}
