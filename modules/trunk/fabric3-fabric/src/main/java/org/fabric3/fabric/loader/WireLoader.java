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
import org.fabric3.spi.loader.InvalidWireException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.WireDefinition;

/**
 * Loads a wire from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class WireLoader extends LoaderExtension<Object, WireDefinition> {
    private static final QName WIRE = new QName(SCA_NS, "wire");
    private static final QName SOURCE_URI = new QName(SCA_NS, "source.uri");
    private static final QName TARGET_URI = new QName(SCA_NS, "target.uri");

    @Constructor
    public WireLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return WIRE;
    }

    public WireDefinition load(Object modelType, XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        assert WIRE.equals(reader.getName());
        WireDefinition wireDefn;
        URI sourceURI = null;
        URI targetURI = null;
        String uriString;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                try {
                    if (reader.getName().equals(SOURCE_URI)) {
                        uriString = reader.getElementText();
                        if (uriString != null && uriString.trim().length() > 0) {
                            QualifiedName name = new QualifiedName(uriString);
                            if (name.getPortName() == null) {
                                sourceURI = new URI(uriString);
                            } else {
                                sourceURI = new URI(name.getPartName() + "#" + name.getPortName());
                            }
                        } else {
                            throw new InvalidWireException("Wire source not defined");
                        }
                    } else if (reader.getName().equals(TARGET_URI)) {
                        uriString = reader.getElementText();
                        if (uriString != null && uriString.trim().length() > 0) {
                            QualifiedName name = new QualifiedName(uriString);
                            if (name.getPortName() == null) {
                                targetURI = new URI(uriString);
                            } else {
                                targetURI = new URI(name.getPartName() + "#" + name.getPortName());
                            }
                        } else {
                            throw new InvalidWireException("Wire target not defined");
                        }
                    } else {
                        QName name = reader.getName();
                        throw new InvalidWireException("Unrecognized element in wire ", name.toString());
                    }
                } catch (URISyntaxException e) {
                    throw new InvalidWireException("Invalid wire uri", e);
                }

                reader.next();
                break;
            case END_ELEMENT:
                if (reader.getName().equals(WIRE)) {
                    if (sourceURI != null && targetURI != null) {
                        wireDefn = new WireDefinition();
                        wireDefn.setSource(sourceURI);
                        wireDefn.setTarget(targetURI);
                    } else {
                        throw new InvalidWireException("Incomplete wire definition");
                    }
                    return wireDefn;
                }
            }
        }
    }
}
