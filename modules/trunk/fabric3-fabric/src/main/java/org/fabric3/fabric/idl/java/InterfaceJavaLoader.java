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
package org.fabric3.fabric.idl.java;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;

/**
 * Loads a Java interface definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class InterfaceJavaLoader extends LoaderExtension<JavaServiceContract> {
    public static final QName INTERFACE_JAVA = new QName(SCA_NS, "interface.java");

    private final JavaInterfaceProcessorRegistry interfaceRegistry;

    @Constructor({"registry", "interfaceRegistry"})
    public InterfaceJavaLoader(@Reference LoaderRegistry registry,
                               @Reference JavaInterfaceProcessorRegistry interfaceRegistry) {
        super(registry);
        this.interfaceRegistry = interfaceRegistry;
    }

    public QName getXMLType() {
        return INTERFACE_JAVA;
    }

    public JavaServiceContract load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        assert INTERFACE_JAVA.equals(reader.getName());
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
        Class<?> interfaceClass = LoaderUtil.loadClass(name, loaderContext.getTargetClassLoader());

        name = reader.getAttributeValue(null, "callbackInterface");
        Class<?> callbackClass = (name != null) ? LoaderUtil.loadClass(name, loaderContext.getTargetClassLoader()) : null;

        LoaderUtil.skipToEndElement(reader);

        JavaServiceContract serviceContract;
        try {
            serviceContract = interfaceRegistry.introspect(interfaceClass, callbackClass);
        } catch (InvalidServiceContractException e) {
            throw new LoaderException(interfaceClass.getName(), e);
        }

        serviceContract.setConversational(conversational);
        return serviceContract;
    }
}
