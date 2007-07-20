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
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.Constants;

import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.model.type.BindingDefinition;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.UnrecognizedElementException;

/**
 * Loads a service definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentServiceLoader implements StAXElementLoader<ServiceDefinition> {
    private static final QName SERVICE = new QName(Constants.SCA_NS, "Service");

    private final LoaderRegistry registry;

    public ComponentServiceLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
    }

    public QName getXMLType() {
        return SERVICE;
    }

    public ServiceDefinition load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, "name");
        ServiceDefinition def = new ServiceDefinition();
        try {
            def.setUri(new URI('#' + name));
        } catch (URISyntaxException e) {
            LoaderException le = new LoaderException(e);
            le.setLine(reader.getLocation().getLineNumber());
            le.setColumn(reader.getLocation().getColumnNumber());
            throw le;
        }

        URI targetUri = null;
        String promote = reader.getAttributeValue(null, "promote");
        if (promote != null) {
            QualifiedName qName = new QualifiedName(promote);
            try {
                targetUri = new URI(qName.getFragment());
            } catch (URISyntaxException e) {
                LoaderException le = new LoaderException(e);
                le.setLine(reader.getLocation().getLineNumber());
                le.setColumn(reader.getLocation().getColumnNumber());
                throw le;
            }
        }
        while (true) {
            int i = reader.next();
            switch (i) {
            case XMLStreamConstants.START_ELEMENT:
                ModelObject type = registry.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    def.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    def.addBinding((BindingDefinition) type);
                } else {
                    throw new UnrecognizedElementException(reader.getName());
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (targetUri != null) {
                    def.setTarget(targetUri);
                }
                return def;
            }
        }
    }
}
