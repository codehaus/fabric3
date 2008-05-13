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
package org.fabric3.binding.rmi.model.logical;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;

@EagerInit
public class RmiBindingLoader implements TypeLoader<RmiBindingDefinition> {

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName(SCA_NS, "binding.rmi");

    public RmiBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {
        String uri = reader.getAttributeValue(null, "uri");
        String name = reader.getAttributeValue(null, "name");
        assert name != null && name.length() > 0;
        String serviceName = reader.getAttributeValue(null, "serviceName");
        URI targetURI;
        if (uri != null) {
            targetURI = URI.create(uri);
        } else {
            String target = serviceName != null ? serviceName : name;
            targetURI = URI.create(target);
        }

        RmiBindingDefinition definition = new RmiBindingDefinition(targetURI);
        definition.setName(name);
        if (serviceName != null) {
            definition.setServiceName(serviceName);
        } else {
            definition.setServiceName(name);
        }
        String attribute = reader.getAttributeValue(null, "host");
        if (attribute != null) {
            definition.setHost(attribute);
        }
        attribute = reader.getAttributeValue(null, "port");
        if (attribute != null) {
            definition.setPort(Integer.parseInt(attribute));
        }
        LoaderUtil.skipToEndElement(reader);
        return definition;
    }

}
