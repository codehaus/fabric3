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
package org.fabric3.junit.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.junit.scdl.JUnitImplementation;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JUnitImplementationLoader implements TypeLoader<JUnitImplementation> {

    private final JUnitImplementationProcessor implementationProcessor;

    public JUnitImplementationLoader(@Reference JUnitImplementationProcessor implementationProcessor) {
        this.implementationProcessor = implementationProcessor;
    }

    public JUnitImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {
        String className = reader.getAttributeValue(null, "class");
        LoaderUtil.skipToEndElement(reader);

        JUnitImplementation impl = new JUnitImplementation(className);
        try {
            implementationProcessor.introspect(impl, introspectionContext);
        } catch (IntrospectionException e) {
            throw new LoaderException(reader, e);
        }
        return impl;
    }

}
