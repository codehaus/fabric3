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
package org.fabric3.loader.common;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * Loads a Java interface definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class JavaInterfaceLoader implements StAXElementLoader<ServiceContract> {

    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public JavaInterfaceLoader(@Reference ContractProcessor contractProcessor,
                               @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public ServiceContract load(XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, LoaderException {

        String conversationalAttr = reader.getAttributeValue(null, "conversational");
        boolean conversational = Boolean.parseBoolean(conversationalAttr);
        String name = reader.getAttributeValue(null, "interface");
        if (name == null) {
            // allow "class" as well as seems to be a common mistake
            name = reader.getAttributeValue(null, "class");
        }
        if (name == null) {
            throw new InvalidValueException("interface name not supplied");
        }
        Class<?> interfaceClass = LoaderUtil.loadClass(name, context.getTargetClassLoader());

        name = reader.getAttributeValue(null, "callbackInterface");
        Class<?> callbackClass =
                (name != null) ? LoaderUtil.loadClass(name, context.getTargetClassLoader()) : null;

        LoaderUtil.skipToEndElement(reader);

        try {
            TypeMapping typeMapping = helper.mapTypeParameters(interfaceClass);
            ServiceContract<?> serviceContract = contractProcessor.introspect(typeMapping, interfaceClass);
            if (callbackClass != null) {
                ServiceContract<?> callbackContract = contractProcessor.introspect(typeMapping, callbackClass);
                serviceContract.setCallbackContract(callbackContract);
            }
            serviceContract.setConversational(conversational);
            return serviceContract;
        } catch (InvalidServiceContractException e) {
            throw new LoaderException("The Java interface is an invalid service contract: " + interfaceClass.getName(), e);
        }
    }
}
