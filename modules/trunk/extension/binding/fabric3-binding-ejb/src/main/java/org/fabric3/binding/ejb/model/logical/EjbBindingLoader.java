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
package org.fabric3.binding.ejb.model.logical;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;


/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
@EagerInit
public class EjbBindingLoader implements StAXElementLoader<EjbBindingDefinition> {

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName(SCA_NS, "binding.ejb");

    private LoaderRegistry registry;

    public EjbBindingLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void start() {
        registry.registerLoader(BINDING_QNAME, this);
    }

    @Destroy
    public void stop() {
        registry.unregisterLoader(BINDING_QNAME);
    }


    public EjbBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {

        String uri = reader.getAttributeValue(null, "uri");

        EjbBindingDefinition bd = new EjbBindingDefinition(createURI(uri));

        String homeInterface = reader.getAttributeValue(null, "homeInterface");
        bd.setHomeInterface(homeInterface);

        bd.setEjbLink(reader.getAttributeValue(null, "ejb-link-name"));

        if ("stateful".equalsIgnoreCase(reader.getAttributeValue(null, "session-type"))) {
            bd.setStateless(false);
        }

        boolean isEjb3 = true;
        String ejbVersion = reader.getAttributeValue(null, "ejb-version");
        if (ejbVersion != null) {
            isEjb3 = "EJB3".equalsIgnoreCase(ejbVersion);
        } else {
            isEjb3 = (homeInterface == null);
        }
        bd.setEjb3(isEjb3);

        if (!isEjb3 && homeInterface == null) {
            throw new LoaderException("homeInterface must be specified for EJB 2.x bindings");
        }

        bd.setName(reader.getAttributeValue(null, "name"));


        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

    private URI createURI(String uri) throws LoaderException {
        if (uri == null) return null;

        // In EJB 3, the @Stateless & @Stateful annotations contain an attribute named mappedName.
        // Although the specification doesn't spell out what this attribute is used for, it is
        // commonly used to specify a JNDI name for the EJB.  However, EJB 3 beans can have multiple
        // interfaces.  As a result, most containers including Glassfish and WebLogic calculate a JNDI
        // name for each interface based on the mappedName.  In both Glassfish and WebLogic, the JNDI
        // name for each interface is calculated using the following formula:
        // <mappedName>#<fully qualified interface name>
        // The problem is that the '#' char is a URI fragment delimitor and therefore can't legally be used
        // in a URI.  Constructing a URI from such a JNDI name leads to an URISyntaxException being thrown.
        // As such, we'll attempt to account for this issue by stripping off the "corbaname:rir:#" portion
        // of the URI string before we actually construct the URI object.

        if (uri.indexOf('#') != uri.lastIndexOf('#')) {
            if (uri.startsWith("corbaname:rir:#")) {
                uri = uri.substring(uri.indexOf('#') + 1);
            }
        }

        try {
            return new URI(uri);
        } catch (URISyntaxException ex) {
            throw new LoaderException(ex);
        }
    }

}
