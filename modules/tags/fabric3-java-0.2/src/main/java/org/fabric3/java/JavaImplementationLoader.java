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
package org.fabric3.java;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.InvalidValueException;

public class JavaImplementationLoader implements StAXElementLoader<JavaImplementation> {

    private final JavaComponentTypeLoader componentTypeLoader;
    private final PolicyHelper policyHelper;


    public JavaImplementationLoader(@Reference JavaComponentTypeLoader componentTypeLoader,
                                    @Reference PolicyHelper policyHelper) {
        this.componentTypeLoader = componentTypeLoader;
        this.policyHelper = policyHelper;
    }


    public JavaImplementation load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        assert JavaImplementation.IMPLEMENTATION_JAVA.equals(reader.getName());

        JavaImplementation implementation = new JavaImplementation();
        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            throw new InvalidValueException("Missing implementation class");
        }
        policyHelper.loadPolicySetsAndIntents(implementation, reader);
        LoaderUtil.skipToEndElement(reader);

        implementation.setImplementationClass(implClass);
        componentTypeLoader.load(implementation, loaderContext);
        return implementation;
    }

}
