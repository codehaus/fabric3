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
package org.fabric3.fabric.implementation.java;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;

public class JavaImplementationLoader extends LoaderExtension<JavaImplementation> {
    public static final QName IMPLEMENTATION_JAVA = new QName(SCA_NS, "implementation.java");

    private final JavaComponentTypeLoader componentTypeLoader;
    private final PolicyHelper policyHelper;


    public JavaImplementationLoader(@Reference LoaderRegistry registry,
                                    @Reference JavaComponentTypeLoader componentTypeLoader,
                                    @Reference PolicyHelper policyHelper) {
        super(registry);
        this.componentTypeLoader = componentTypeLoader;
        this.policyHelper = policyHelper;
    }

    @Override
    public QName getXMLType() {
        return IMPLEMENTATION_JAVA;
    }

    public JavaImplementation load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        
        assert IMPLEMENTATION_JAVA.equals(reader.getName());
        
        JavaImplementation implementation = new JavaImplementation();
        String implClass = reader.getAttributeValue(null, "class");
        
        policyHelper.loadPolicySetsAndIntents(implementation, reader);
        LoaderUtil.skipToEndElement(reader);

        Class<?> implementationClass = LoaderUtil.loadClass(implClass, loaderContext.getTargetClassLoader());
        implementation.setClassName(implClass);
        implementation.setImplementationClass(implementationClass);
        componentTypeLoader.load(implementation,  loaderContext);
        return implementation;
    }

}
