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
package org.fabric3.fabric.loader;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.UnrecognizedElementException;
import org.fabric3.spi.model.type.BindingDefinition;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.model.type.ServiceDefinition;

/**
 * Loads a service definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ServiceLoader extends LoaderExtension<ServiceDefinition> {
    private static final QName SERVICE = new QName(SCA_NS, "service");

    @Constructor
    public ServiceLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return SERVICE;
    }

    public ServiceDefinition load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        assert SERVICE.equals(reader.getName());
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
            case START_ELEMENT:
                ModelObject type = registry.load(reader, context);
                if (type instanceof ServiceContract) {
                    def.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    def.addBinding((BindingDefinition) type);
                } else {
                    throw new UnrecognizedElementException(reader.getName());
                }
                break;
            case END_ELEMENT:
                if (SERVICE.equals(reader.getName())) {
                    if (targetUri != null) {
                        def.setTarget(targetUri);
                    }
                    return def;
                }
                break;
            }
        }
    }
}
