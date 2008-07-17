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
package org.fabric3.java.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.java.scdl.JavaImplementation;

/**
 * Loads <implementation.java> in a composite.
 */
public class JavaImplementationLoader implements TypeLoader<JavaImplementation> {

    private final JavaImplementationProcessor implementationProcessor;
    private final LoaderHelper loaderHelper;


    public JavaImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference LoaderHelper loaderHelper) {
        this.implementationProcessor = implementationProcessor;
        this.loaderHelper = loaderHelper;
    }


    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        assert JavaImplementation.IMPLEMENTATION_JAVA.equals(reader.getName());

        JavaImplementation implementation = new JavaImplementation();
        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("The class attribute was not specified", "class", reader);
            introspectionContext.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return implementation;
        }
        loaderHelper.loadPolicySetsAndIntents(implementation, reader, introspectionContext);

        LoaderUtil.skipToEndElement(reader);

        implementation.setImplementationClass(implClass);
        implementationProcessor.introspect(implementation, introspectionContext);
        return implementation;
    }

}
