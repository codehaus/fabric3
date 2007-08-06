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
package org.fabric3.binding.ws.model.logical;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class WsBindingLoader extends LoaderExtension<WsBindingDefinition> {

    /** Qualified name for the binding element. */
    private static final QName BINDING_QNAME =  new QName(Constants.SCA_NS, "binding.ws");
    
    private final PolicyHelper policyHelper;

    /**
     * Injects the registry.
     * @param registry Loader registry.
     */
    public WsBindingLoader(@Reference LoaderRegistry registry, @Reference PolicyHelper policyHelper) {
        super(registry);
        this.policyHelper = policyHelper;
    }

    /**
     * @see org.fabric3.extension.loader.LoaderExtension#getXMLType()
     */
    @Override
    public QName getXMLType() {
        return BINDING_QNAME;
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(java.lang.Object, javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public WsBindingDefinition load(XMLStreamReader reader, LoaderContext loaderContext)
        throws XMLStreamException, LoaderException {

        WsBindingDefinition bd = new WsBindingDefinition();

        try {

            String uri = reader.getAttributeValue(null, "uri");
            if(uri == null) {
                throw new LoaderException("The uri attribute is not specified");
            }
            bd.setTargetUri(new URI(uri));
            
            policyHelper.loadPolicySetsAndIntents(bd, reader);

            // TODO Add rest of the WSDL support

        } catch(URISyntaxException ex) {
            throw new LoaderException(ex);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

}
