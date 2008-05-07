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
package org.fabric3.jpa.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.jpa.scdl.JpaImplementation;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation loader for JPA component.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class JpaImplementationLoader implements TypeLoader<JpaImplementation> {
    
    private LoaderRegistry loaderRegistry;
    
    /**
     * Initializes the loader registry.
     * 
     * @param loaderRegistry Loader registry.
     */
    public JpaImplementationLoader(@Reference LoaderRegistry loaderRegistry) {
        this.loaderRegistry = loaderRegistry;
    }
    
    /**
     * Self registers with the registry.
     */
    @Init
    public void start() {
        loaderRegistry.registerLoader(JpaImplementation.IMPLEMENTATION_JPA, this);
    }

    /**
     * Creates the instance of the implementation type.
     * 
     * @param reader Stax XML stream reader used for reading data.
     * @param context Introspection context.
     * @return An instance of the JPA implemenation.
     */
    public JpaImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {
        
        String persistenceUnit = reader.getAttributeValue(null, "persistenceUnit");
        if (persistenceUnit == null) {
            throw new LoaderException("Missing attribute: persistenceUnit");
        }
        return new JpaImplementation(persistenceUnit);
        
    }

}
