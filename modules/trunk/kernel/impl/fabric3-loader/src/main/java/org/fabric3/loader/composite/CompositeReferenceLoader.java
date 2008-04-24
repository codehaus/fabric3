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
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.OperationDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.introspection.xml.InvalidValueException;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedElementException;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class CompositeReferenceLoader implements TypeLoader<CompositeReference> {
    private static final QName CALLBACK = new QName(SCA_NS, "callback");
    private final Loader loader;
    private final LoaderHelper loaderHelper;

    public CompositeReferenceLoader(@Reference Loader loader, @Reference LoaderHelper loaderHelper) {
        this.loader = loader;
        this.loaderHelper = loaderHelper;
    }

    public CompositeReference load(XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            throw new InvalidValueException("Reference name not specified", name);
        }

        List<URI> promotedUris = loaderHelper.parseListOfUris(reader, "promote");
        CompositeReference referenceDefinition = new CompositeReference(name, promotedUris);
        loaderHelper.loadPolicySetsAndIntents(referenceDefinition, reader);

        try {
            Multiplicity multiplicity = Multiplicity.fromString(reader.getAttributeValue(null, "multiplicity"));
            referenceDefinition.setMultiplicity(multiplicity);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException(reader.getAttributeValue(null, "multiplicity"), "multiplicity");
        }
        boolean callback = false;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                ModelObject type = loader.load(reader, ModelObject.class, context);
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
                } else {
                    throw new UnrecognizedElementException(reader.getName());
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
}
