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
package org.fabric3.binding.ws.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class WsBindingLoader implements TypeLoader<WsBindingDefinition> {

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.ws");

    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public WsBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public WsBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        WsBindingDefinition bd = null;
        String uri = null;
        try {

            uri = reader.getAttributeValue(null, "uri");
            String implementation = reader.getAttributeValue(org.fabric3.spi.Constants.FABRIC3_NS, "impl");
            String wsdlElement = reader.getAttributeValue(null, "wsdlElement");
            String wsdlLocation = reader.getAttributeValue("http://www.w3.org/2004/08/wsdl-instance", "wsdlLocation");

            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("The uri attribute is not specified", "uri", reader);
                introspectionContext.addError(failure);
                bd = new WsBindingDefinition(null, implementation, wsdlLocation, wsdlElement);
            } else {
                bd = new WsBindingDefinition(new URI(uri), implementation, wsdlLocation, wsdlElement);
            }
            loaderHelper.loadPolicySetsAndIntents(bd, reader, introspectionContext);

            // TODO Add rest of the WSDL support

        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The web services binding URI is not a valid: " + uri, "uri", reader);
            introspectionContext.addError(failure);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

}
