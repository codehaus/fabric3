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
package org.fabric3.loader.composite;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.OperationDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.UnrecognizedElementException;

/**
 * Loads a service definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class CompositeServiceLoader implements StAXElementLoader<CompositeService> {
    private final Loader loader;
    private final PolicyHelper policyHelper;

    public CompositeServiceLoader(@Reference Loader loader, @Reference PolicyHelper policyHelper) {
        this.loader = loader;
        this.policyHelper = policyHelper;
    }

    public CompositeService load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            throw new InvalidValueException("Service name not specified", name);
        }
        String promote = reader.getAttributeValue(null, "promote");
        if (promote == null) {
            throw new InvalidValueException("Promote not specified", name);
        }

        CompositeService def = new CompositeService(name, null);
        def.setPromote(LoaderUtil.getURI(promote));

        policyHelper.loadPolicySetsAndIntents(def, reader);
        while (true) {
            int i = reader.next();
            switch (i) {
            case START_ELEMENT:
                ModelObject type = loader.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    def.setServiceContract((ServiceContract<?>) type);
                } else if (type instanceof BindingDefinition) {
                    def.addBinding((BindingDefinition) type);
                } else if (type instanceof OperationDefinition) {
                    def.addOperation((OperationDefinition) type);
                } else {
                    throw new UnrecognizedElementException(reader.getName());
                }
                break;
            case END_ELEMENT:
                return def;
            }
        }
    }
}
