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

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.ImplementationNotFoundException;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.ResourceNotFound;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.ServiceContract;

/**
 * Loads a Java interface definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class JavaInterfaceLoader implements TypeLoader<ServiceContract> {

    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public JavaInterfaceLoader(@Reference ContractProcessor contractProcessor,
                               @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public ServiceContract load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "interface");
        if (name == null) {
            // allow "class" as well as seems to be a common mistake
            name = reader.getAttributeValue(null, "class");
        }
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("An interface must be specified using the class attribute", "class", reader);
            context.addError(failure);
            return null;
        }
        Class<?> interfaceClass;
        try {
            interfaceClass = helper.loadClass(name, context.getTargetClassLoader());
        } catch (ImplementationNotFoundException e) {
            ResourceNotFound failure = new ResourceNotFound("Interface not found: " + name, name, reader);
            context.addError(failure);
            return null;
        }

        name = reader.getAttributeValue(null, "callbackInterface");
        Class<?> callbackClass;
        try {
            callbackClass = (name != null) ? helper.loadClass(name, context.getTargetClassLoader()) : null;
        } catch (ImplementationNotFoundException e) {
            ResourceNotFound failure = new ResourceNotFound("Callback interface not found: " + name, name, reader);
            context.addError(failure);
            return null;
        }

        LoaderUtil.skipToEndElement(reader);

        TypeMapping typeMapping = helper.mapTypeParameters(interfaceClass);
        ServiceContract<?> serviceContract = contractProcessor.introspect(typeMapping, interfaceClass, context);
        if (callbackClass != null) {
            ServiceContract<?> callbackContract = contractProcessor.introspect(typeMapping, callbackClass, context);
            serviceContract.setCallbackContract(callbackContract);
        }
        return serviceContract;
    }
}
