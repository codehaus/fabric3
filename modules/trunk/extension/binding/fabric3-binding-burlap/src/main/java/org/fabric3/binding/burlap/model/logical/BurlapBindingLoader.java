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
package org.fabric3.binding.burlap.model.logical;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class BurlapBindingLoader implements StAXElementLoader<BurlapBindingDefinition> {

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName("http://www.fabric3.org/binding/burlap/0.2", "binding.burlap");

    private LoaderRegistry registry;
    private final PolicyHelper policyHelper;


    /**
     * Constructor.
     *
     * @param registry     Loader registry.
     * @param policyHelper the policy helper
     */
    public BurlapBindingLoader(@Reference LoaderRegistry registry, @Reference PolicyHelper policyHelper) {
        this.registry = registry;
        this.policyHelper = policyHelper;
    }

    @Init
    public void start() {
        registry.registerLoader(BINDING_QNAME, this);
    }

    @Destroy
    public void stop() {
        registry.unregisterLoader(BINDING_QNAME);
    }

    public BurlapBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {

        BurlapBindingDefinition bd;

        try {

            String uri = reader.getAttributeValue(null, "uri");
            if (uri == null) {
                throw new LoaderException("The uri attribute is not specified");
            }
            bd = new BurlapBindingDefinition(new URI(uri));

            policyHelper.loadPolicySetsAndIntents(bd, reader);

        } catch (URISyntaxException ex) {
            throw new LoaderException(ex);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

}
