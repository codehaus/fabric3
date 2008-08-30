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
package org.fabric3.binding.tcp.introspection;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.tcp.scdl.TCPBindingDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * @version $Revision$ $Date$
 */
public class TCPBindingLoader implements TypeLoader<TCPBindingDefinition> {

    /**
     * {@inheritDoc}
     */
    public TCPBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException {

        TCPBindingDefinition bd = null;
        String uri = null;

        try {

            uri = reader.getAttributeValue(null, "uri");
            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("A binding URI must be specified ", "uri", reader);
                introspectionContext.addError(failure);
                return null;
            }
            if (!uri.contains("://")) { // Default to TCP.
                uri = "tcp://" + uri;
            }

            bd = new TCPBindingDefinition(new URI(uri));

        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The TCP binding URI is not valid: " + uri, "uri", reader);
            introspectionContext.addError(failure);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

}
