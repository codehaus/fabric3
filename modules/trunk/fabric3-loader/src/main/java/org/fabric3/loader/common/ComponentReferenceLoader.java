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

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.Multiplicity;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.model.type.BindingDefinition;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.UnrecognizedElementException;
import org.fabric3.spi.loader.InvalidReferenceException;
import org.fabric3.spi.Constants;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentReferenceLoader implements StAXElementLoader<ReferenceDefinition> {
    private static final QName REFERENCE = new QName(org.osoa.sca.Constants.SCA_NS, "Reference");
    private static final Map<String, Multiplicity> MULTIPLICITY = new HashMap<String, Multiplicity>(4);

    private final LoaderRegistry registry;

    static {
        MULTIPLICITY.put("0..1", Multiplicity.ZERO_ONE);
        MULTIPLICITY.put("1..1", Multiplicity.ONE_ONE);
        MULTIPLICITY.put("0..n", Multiplicity.ZERO_N);
        MULTIPLICITY.put("1..n", Multiplicity.ONE_N);
    }

    public ComponentReferenceLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
    }

    public QName getXMLType() {
        return REFERENCE;
    }

    public ReferenceDefinition load(XMLStreamReader reader, LoaderContext context)
        throws XMLStreamException, LoaderException {

        ReferenceDefinition referenceDefinition = new ReferenceDefinition();

        String name = reader.getAttributeValue(null, "name");

        setPromoted(reader, referenceDefinition, name);

        setMultiplicity(reader, referenceDefinition);

        setUri(reader, referenceDefinition, name);

        setKey(reader, referenceDefinition);

        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    ModelObject type = registry.load(reader, ModelObject.class, context);
                    if (type instanceof ServiceContract) {
                        referenceDefinition.setServiceContract((ServiceContract)type);
                    } else if (type instanceof BindingDefinition) {
                        referenceDefinition.addBinding((BindingDefinition)type);
                    } else {
                        throw new UnrecognizedElementException(reader.getName());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    return referenceDefinition;
            }
        }

    }

    /*
     * Processes the key attribute for map references.
     */
    private void setKey(XMLStreamReader reader, ReferenceDefinition referenceDefinition) {

        String key = reader.getAttributeValue(Constants.FABRIC3_NS, "key");
        if(key != null) {
            referenceDefinition.setKey(key);
        }

    }

    /*
     * Sets the reference URI.
     */
    private void setUri(XMLStreamReader reader, ReferenceDefinition referenceDefinition, String name)
        throws LoaderException {

        try {
            referenceDefinition.setUri(new URI('#' + name));
        } catch (URISyntaxException e) {
            LoaderException le = new LoaderException(e);
            le.setLine(reader.getLocation().getLineNumber());
            le.setColumn(reader.getLocation().getColumnNumber());
            throw le;
        }

    }

    /*
     * Processes the multiplicty attribute.
     */
    private void setMultiplicity(XMLStreamReader reader, ReferenceDefinition referenceDefinition) {

        String multiplicityVal = reader.getAttributeValue(null, "multiplicity");
        Multiplicity multiplicity = multiplicity(multiplicityVal, Multiplicity.ONE_ONE);
        referenceDefinition.setMultiplicity(multiplicity);

    }

    /*
     * Processes the promotes attribute.
     */
    private void setPromoted(XMLStreamReader reader, ReferenceDefinition referenceDefinition, String name)
        throws InvalidReferenceException, InvalidNameException {

        String promoted = reader.getAttributeValue(null, "promote");
        if (promoted == null || promoted.trim().length() < 1) {
            throw new InvalidReferenceException("No promoted reference specified", name);
        }
        StringTokenizer tokenizer = new StringTokenizer(promoted, " ");
        while (tokenizer.hasMoreTokens()) {
            try {
                QualifiedName qName = new QualifiedName(tokenizer.nextToken());
                URI uri;
                if (qName.getPartName() != null) {
                    uri = new URI(qName.getPartName() + "#" + qName.getPortName());
                } else {
                    uri = new URI(qName.getPartName() + "#" + qName.getPortName());

                }
                referenceDefinition.addPromoted(uri);
            } catch (URISyntaxException e) {
                throw new InvalidReferenceException("Invalid promoted uri", name, e);
            }
        }

    }

    /**
     * Convert a "multiplicity" attribute to the equivalent enum value.
     *
     * @param multiplicity the attribute to convert
     * @param def the default value
     * @return the enum equivalent
     */
    private static Multiplicity multiplicity(String multiplicity, Multiplicity def) {
        return multiplicity == null ? def : MULTIPLICITY.get(multiplicity);
    }

}
