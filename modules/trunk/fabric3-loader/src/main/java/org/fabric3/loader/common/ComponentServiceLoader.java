/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.UnrecognizedElementException;

/**
 * Loads a service definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentServiceLoader implements StAXElementLoader<ServiceDefinition> {
    private final Loader loader;
    private final PolicyHelper policyHelper;

    public ComponentServiceLoader(@Reference Loader loader,
                                  @Reference PolicyHelper policyHelper) {
        this.loader = loader;
        this.policyHelper = policyHelper;
    }

    public ServiceDefinition load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        
        String name = reader.getAttributeValue(null, "name");
        ServiceDefinition def = new ServiceDefinition(name, null);
        
        policyHelper.loadPolicySetsAndIntents(def, reader);

        while (true) {
            int i = reader.next();
            switch (i) {
            case XMLStreamConstants.START_ELEMENT:
                ModelObject type = loader.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    def.setServiceContract((ServiceContract<?>) type);
                } else if (type instanceof BindingDefinition) {
                    def.addBinding((BindingDefinition) type);
                } else {
                    throw new UnrecognizedElementException(reader.getName());
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                return def;
            }
        }
    }
}
