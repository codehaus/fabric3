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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedElement;
import org.fabric3.introspection.xml.UnrecognizedElementException;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.OperationDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentReferenceLoader implements TypeLoader<ComponentReference> {
    private static final QName CALLBACK = new QName(SCA_NS, "callback");

    private final Loader loader;
    private final LoaderHelper loaderHelper;

    public ComponentReferenceLoader(@Reference Loader loader, @Reference LoaderHelper loaderHelper) {
        this.loader = loader;
        this.loaderHelper = loaderHelper;
    }

    public ComponentReference load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingReferenceName failure = new MissingReferenceName(reader);
            context.addError(failure);
            return null;
        }
        ComponentReference reference = new ComponentReference(name);

        boolean autowire = Boolean.parseBoolean(reader.getAttributeValue(null, "autowire"));
        reference.setAutowire(autowire);

        String value = reader.getAttributeValue(null, "multiplicity");
        try {
            Multiplicity multiplicity = Multiplicity.fromString(value);
            reference.setMultiplicity(multiplicity);
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid multiplicity value: " + value, value, reader);
            context.addError(failure);
        }

        String target = reader.getAttributeValue(null, "target");
        List<URI> uris = new ArrayList<URI>();
        if (target != null) {
            StringTokenizer tokenizer = new StringTokenizer(target);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                uris.add(loaderHelper.getURI(token));
            }
        }
        reference.getTargets().addAll(uris);

        loaderHelper.loadPolicySetsAndIntents(reference, reader, context);

        boolean callback = false;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                ModelObject type;
                try {
                    type = loader.load(reader, ModelObject.class, context);
                    // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
                    // UnrecognizedElement added to the context if none is found
                } catch (UnrecognizedElementException e) {
                    UnrecognizedElement failure = new UnrecognizedElement(reader);
                    context.addError(failure);
                    continue;
                }
                if (type instanceof ServiceContract) {
                    reference.setServiceContract((ServiceContract<?>) type);
                } else if (type instanceof BindingDefinition) {
                    if (callback) {
                        reference.addCallbackBinding((BindingDefinition) type);
                    } else {
                        reference.addBinding((BindingDefinition) type);
                    }
                } else if (type instanceof OperationDefinition) {
                    reference.addOperation((OperationDefinition) type);
                }else if (type == null) {
                    // error loading, the element, ignore as an error will have been reported
                    break;
                } else {
                    context.addError(new UnrecognizedElement(reader));
                    continue;
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (callback) {
                    callback = false;
                    break;
                }
                return reference;
            }
        }
    }
}
