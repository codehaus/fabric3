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
package org.fabric3.loader.composite;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedAttribute;
import org.fabric3.introspection.xml.UnrecognizedElement;
import org.fabric3.introspection.xml.UnrecognizedElementException;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.OperationDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class CompositeReferenceLoader implements TypeLoader<CompositeReference> {
    private static final QName CALLBACK = new QName(SCA_NS, "callback");
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("autowire", "autowire");
        ATTRIBUTES.put("promote", "promote");
        ATTRIBUTES.put("multiplicity", "multiplicity");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
    }

    private final Loader loader;
    private final LoaderHelper loaderHelper;

    public CompositeReferenceLoader(@Reference Loader loader, @Reference LoaderHelper loaderHelper) {
        this.loader = loader;
        this.loaderHelper = loaderHelper;
    }

    public CompositeReference load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Reference name not specified", "name", reader);
            context.addError(failure);
            return null;
        }

        List<URI> promotedUris = loaderHelper.parseListOfUris(reader, "promote");
        CompositeReference referenceDefinition = new CompositeReference(name, promotedUris);
        loaderHelper.loadPolicySetsAndIntents(referenceDefinition, reader, context);

        String value = reader.getAttributeValue(null, "multiplicity");
        try {
            Multiplicity multiplicity = Multiplicity.fromString(value);
            referenceDefinition.setMultiplicity(multiplicity);
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid multiplicity value: " + value, value, reader);
            context.addError(failure);
        }
        boolean callback = false;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                QName elementName = reader.getName();
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
                    referenceDefinition.setServiceContract((ServiceContract<?>) type);
                } else if (type instanceof BindingDefinition) {
                    if (callback) {
                        referenceDefinition.addCallbackBinding((BindingDefinition) type);
                    } else {
                        referenceDefinition.addBinding((BindingDefinition) type);

                    }
                } else if (type instanceof OperationDefinition) {
                    referenceDefinition.addOperation((OperationDefinition) type);
                } else if (type == null) {
                    // there was an error loading the element, ingore it as the errors will have been reported
                    continue;
                } else {
                    context.addError(new UnrecognizedElement(reader));
                    continue;
                }
                if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                    throw new AssertionError("Loader must position the cursor to the end element");
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (callback) {
                    callback = false;
                    break;
                }
                return referenceDefinition;
            }
        }

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
