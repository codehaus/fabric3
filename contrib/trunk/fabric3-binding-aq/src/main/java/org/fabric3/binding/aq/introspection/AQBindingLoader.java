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
package org.fabric3.binding.aq.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.aq.common.InitialState;
import org.fabric3.binding.aq.scdl.AQBindingDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class AQBindingLoader implements TypeLoader<AQBindingDefinition> {

    /** Qualified name for the binding element. */
    public static final QName BINDING_QNAME  =  new QName(Constants.SCA_NS, "binding.aq");  

    private final LoaderHelper loaderHelper;  
              

    /**
     * Constructor
     * @param LoaderHelper
     */
    public AQBindingLoader(final @Reference LoaderHelper loaderHelper) {                  
          this.loaderHelper = loaderHelper;          
    }
    
    /**
     * @throws XMLStreamException 
     * 
     */
    public AQBindingDefinition load(final XMLStreamReader reader, final IntrospectionContext loaderContext) throws XMLStreamException {                
        System.err.println("Inside The Loader");
        final String destinationName = reader.getAttributeValue(null, "destinationName");
        final String sInitialState = reader.getAttributeValue(null, "initialState");
        final String sConsumerCount = reader.getAttributeValue(null, "consumerCount");
        final String dataSourceKey = reader.getAttributeValue(null, "dataSourceKey");
        
        final int consumerCount = sConsumerCount != null ? Integer.parseInt(sConsumerCount) : 0;
        final InitialState initialState = sConsumerCount != null ? InitialState.valueOf(sInitialState) : InitialState.STARTED;
        
        final AQBindingDefinition bindingDefinition = 
            new AQBindingDefinition(destinationName, initialState, dataSourceKey, consumerCount);
        
        loaderHelper.loadPolicySetsAndIntents(bindingDefinition, reader, loaderContext);
        
        LoaderUtil.skipToEndElement(reader);
        
        return bindingDefinition;

    }
    
}
