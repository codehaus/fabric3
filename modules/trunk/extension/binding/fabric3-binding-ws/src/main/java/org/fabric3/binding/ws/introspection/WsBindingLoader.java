/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.ws.introspection;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.host.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class WsBindingLoader implements TypeLoader<WsBindingDefinition> {

    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("uri", "uri");
        ATTRIBUTES.put("impl", "impl");
        ATTRIBUTES.put("wsdlElement", "wsdlElement");
        ATTRIBUTES.put("wsdlLocation", "wsdlLocation");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
    }

    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public WsBindingLoader(@Reference
    LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public WsBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        validateAttributes(reader, introspectionContext);

        WsBindingDefinition bd = null;
        String uri = null;
        try {

            uri = reader.getAttributeValue(null, "uri");
            String implementation = reader.getAttributeValue(Namespaces.IMPLEMENTATION, "impl");
            String wsdlElement = reader.getAttributeValue(null, "wsdlElement");
            String wsdlLocation = reader.getAttributeValue("http://www.w3.org/2004/08/wsdl-instance", "wsdlLocation");

            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("The uri attribute is not specified", reader);
                introspectionContext.addError(failure);
                bd = new WsBindingDefinition(null, implementation, wsdlLocation, wsdlElement, loaderHelper.loadKey(reader));
            } else {
                // encode the URI since there may be expressions (e.g. "${..}") contained in it
                URI endpointUri = new URI(URLEncoder.encode(uri, "UTF-8"));
                bd = new WsBindingDefinition(endpointUri, implementation, wsdlLocation, wsdlElement, loaderHelper.loadKey(reader));
            }
            loaderHelper.loadPolicySetsAndIntents(bd, reader, introspectionContext);

            //Load optional config parameters
            loadConfig(bd, reader);

            // TODO Add rest of the WSDL support

        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The web services binding URI is not a valid: " + uri, reader);
            introspectionContext.addError(failure);
        } catch (UnsupportedEncodingException e) {
            InvalidValue failure = new InvalidValue("Invalid encoding for URI: " + uri + "\n" + e, reader);
            introspectionContext.addError(failure);
        }

        //LoaderUtil.skipToEndElement(reader);
        return bd;

    }

    private void loadConfig(WsBindingDefinition bd, XMLStreamReader reader) throws XMLStreamException {
        Map<String, String> config = null;
        String name = null;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("config".equals(name)) {
                    config = new HashMap<String, String>();
                } else if ("parameter".equals(name)) {
                    final String key = reader.getAttributeValue(null, "name");
                    final String value = reader.getElementText();
                    config.put(key, value);
                }
                break;
            case END_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("config".equals(name)) {
                    bd.setConfig(config);
                } else if ("binding.ws".equals(name)) {
                    return;
                }
                break;
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
