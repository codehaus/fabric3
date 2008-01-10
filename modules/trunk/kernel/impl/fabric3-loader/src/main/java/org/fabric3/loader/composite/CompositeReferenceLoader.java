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
import java.util.StringTokenizer;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.loader.common.InvalidNameException;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.OperationDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.loader.InvalidReferenceException;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.UnrecognizedElementException;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class CompositeReferenceLoader implements StAXElementLoader<CompositeReference> {
    private final Loader loader;
    private final PolicyHelper policyHelper;

    public CompositeReferenceLoader(@Reference Loader loader, @Reference PolicyHelper policyHelper) {
        this.loader = loader;
        this.policyHelper = policyHelper;
    }

    public CompositeReference load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            InvalidValueException e = new InvalidValueException("Reference name not specified", name);
            e.setResourceURI(context.getSourceBase().toString());
            throw e;
        }

        CompositeReference referenceDefinition = new CompositeReference(name, null);
        policyHelper.loadPolicySetsAndIntents(referenceDefinition, reader);

        setPromoted(reader, referenceDefinition, name, context);

        try {
            Multiplicity multiplicity = Multiplicity.fromString(reader.getAttributeValue(null, "multiplicity"));
            referenceDefinition.setMultiplicity(multiplicity);
            if (multiplicity == null
                    || Multiplicity.ONE_ONE.equals(multiplicity)
                    || Multiplicity.ONE_N.equals(multiplicity)) {
                referenceDefinition.setRequired(true);
            }
        } catch (IllegalArgumentException e) {
            InvalidValueException e2 =
                    new InvalidValueException(reader.getAttributeValue(null, "multiplicity"), "multiplicity");
            e2.setResourceURI(context.getSourceBase().toString());
            throw e2;
        }

        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                ModelObject type = loader.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    referenceDefinition.setServiceContract((ServiceContract<?>) type);
                } else if (type instanceof BindingDefinition) {
                    referenceDefinition.addBinding((BindingDefinition) type);
                } else if (type instanceof OperationDefinition) {
                    referenceDefinition.addOperation((OperationDefinition) type);
                } else {
                    UnrecognizedElementException e = new UnrecognizedElementException(reader.getName());
                    e.setResourceURI(context.getSourceBase().toString());
                    throw e;
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                return referenceDefinition;
            }
        }

    }

    /*
     * Processes the promotes attribute.
     */
    private void setPromoted(XMLStreamReader reader,
                             ReferenceDefinition referenceDefinition,
                             String name,
                             LoaderContext context)
            throws InvalidReferenceException, InvalidNameException {

        String promoted = reader.getAttributeValue(null, "promote");
        if (promoted == null || promoted.trim().length() < 1) {
            InvalidReferenceException e = new InvalidReferenceException("No promoted reference specified", name);
            e.setResourceURI(context.getSourceBase().toString());
            throw e;
        }
        StringTokenizer tokenizer = new StringTokenizer(promoted, " ");
        while (tokenizer.hasMoreTokens()) {
            URI uri = LoaderUtil.getURI(tokenizer.nextToken());
            referenceDefinition.addPromoted(uri);
        }

    }
}
