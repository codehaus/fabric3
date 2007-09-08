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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;

import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.spi.loader.InvalidReferenceException;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.UnrecognizedElementException;
import org.fabric3.spi.loader.Loader;

import org.osoa.sca.annotations.Reference;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentReferenceLoader implements StAXElementLoader<ComponentReference> {

    private final Loader loader;
    private final PolicyHelper policyHelper;
    
    public ComponentReferenceLoader(@Reference Loader loader,
                                    @Reference PolicyHelper policyHelper) {
        this.loader = loader;
        this.policyHelper = policyHelper;
    }

    public ComponentReference load(XMLStreamReader reader, LoaderContext context)
        throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            throw new InvalidReferenceException("No name specified");
        }
        ComponentReference reference = new ComponentReference(name);
        
        boolean autowire = Boolean.parseBoolean(reader.getAttributeValue(null, "autowire"));
        reference.setAutowire(autowire);
        
        try {
            Multiplicity multiplicity = Multiplicity.fromString(reader.getAttributeValue(null, "multiplicity"));
            reference.setMultiplicity(multiplicity);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException(reader.getAttributeValue(null, "multiplicity"), "multiplicity");
        }

        String target = reader.getAttributeValue(null, "target");
        List<URI> uris = new ArrayList<URI>();
        if (target != null) {
            StringTokenizer tokenizer = new StringTokenizer(target);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                uris.add(LoaderUtil.getURI(token));
            }
        }
        reference.getTargets().addAll(uris);
        
        policyHelper.loadPolicySetsAndIntents(reference, reader);
        
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    ModelObject type = loader.load(reader, ModelObject.class, context);
                    if (type instanceof ServiceContract) {
                        reference.setServiceContract((ServiceContract<?>)type);
                    } else if (type instanceof BindingDefinition) {
                        reference.addBinding((BindingDefinition)type);
                    } else {
                        throw new UnrecognizedElementException(reader.getName());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    return reference;
            }
        }
    }
}
