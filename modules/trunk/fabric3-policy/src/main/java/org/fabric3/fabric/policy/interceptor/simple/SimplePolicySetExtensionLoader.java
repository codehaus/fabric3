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
package org.fabric3.fabric.policy.interceptor.simple;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.Constants;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * XML loader for the simple interceptor language.
 * 
 * @version $Revision$ $Date$
 */
public class SimplePolicySetExtensionLoader implements StAXElementLoader<SimplePolicySetExtension> {
    
    // Qualified name of the handled element
    private static final QName QNAME = new QName(Constants.FABRIC3_NS, "interceptor");
    
    /**
     * Registers with the loader registry.
     * 
     * @param registry Loader registry.
     */
    public SimplePolicySetExtensionLoader(LoaderRegistry registry) {
        registry.registerLoader(QNAME, this);
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public SimplePolicySetExtension load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        
        String interceptorClass = reader.getAttributeValue(null, "class");
        LoaderUtil.skipToEndElement(reader);

        return new SimplePolicySetExtension(interceptorClass);
        
    }

}
