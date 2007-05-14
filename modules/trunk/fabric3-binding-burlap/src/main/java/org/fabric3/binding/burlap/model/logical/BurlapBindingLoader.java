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
package org.fabric3.binding.burlap.model.logical;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class BurlapBindingLoader extends LoaderExtension<Object, BurlapBindingDefinition> {

    /** Qualified name for the binding element. */
    private static final QName BINDING_QNAME = 
        new QName("http://www.fabric3.org/binding/burlap/0.1-SNAPSHOT", "binding.burlap");
    
    /**
     * Injects the registry.
     * @param registry Loader registry.
     */
    public BurlapBindingLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    @Override
    public QName getXMLType() {
        return BINDING_QNAME;
    }

    public BurlapBindingDefinition load(Object configuration, XMLStreamReader reader, LoaderContext loaderContext)
        throws XMLStreamException, LoaderException {
        
        BurlapBindingDefinition bd = new BurlapBindingDefinition();
        
        try {

            String uri = reader.getAttributeValue(null, "uri");
            if(uri == null) {
                throw new LoaderException("The uri attribute is not specified");
            }
            bd.setTargetUri(new URI(uri));
            
        } catch(URISyntaxException ex) {
            throw new LoaderException(ex);
        }
        
        LoaderUtil.skipToEndElement(reader);
        return bd;
        
    }

}
