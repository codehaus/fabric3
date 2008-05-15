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
package org.fabric3.runtime.standalone.host.implementation.launched;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class LaunchedLoader implements TypeLoader<Launched> {

    private final LaunchedComponentTypeLoader componentTypeLoader;

    public LaunchedLoader(@Reference LaunchedComponentTypeLoader componentTypeLoader) {
        this.componentTypeLoader = componentTypeLoader;
    }


    public Launched load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException, LoaderException {
        String className = reader.getAttributeValue(null, "class");
        String factoryName = reader.getAttributeValue(null, "factory");
        LoaderUtil.skipToEndElement(reader);

        Launched impl = new Launched(className, factoryName);
        componentTypeLoader.load(impl, introspectionContext);
        return impl;
    }
}
