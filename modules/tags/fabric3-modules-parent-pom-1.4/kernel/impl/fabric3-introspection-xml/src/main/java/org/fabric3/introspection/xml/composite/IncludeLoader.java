/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loader that handles &lt;include&gt; elements.
 *
 * @version $Rev$ $Date$
 */
public class IncludeLoader extends AbstractExtensibleTypeLoader<Include> {
    private static final QName INCLUDE = new QName(Constants.SCA_NS, "include");

    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("scdlLocation", "scdlLocation");
        ATTRIBUTES.put("scdlResource", "scdlResource");
        ATTRIBUTES.put("requires", "requires");
    }

    private MetaDataStore store;

    /**
     * Constructor used during boostrap.
     *
     * @param registry the loader registry
     */
    public IncludeLoader(LoaderRegistry registry) {
        super(registry);
    }

    /**
     * Constructor.
     *
     * @param registry the loader registry
     * @param store    optional MetaDataStore used to resolve resources reference to by their symbolic name
     */
    @Constructor
    public IncludeLoader(@Reference LoaderRegistry registry, @Reference(required = false) MetaDataStore store) {
        super(registry);
        this.store = store;
    }

    public QName getXMLType() {
        return INCLUDE;
    }

    public Include load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);

        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", reader);
            context.addError(failure);
            return null;
        }
        QName name = LoaderUtil.getQName(nameAttr, context.getTargetNamespace(), reader.getNamespaceContext());
        String scdlLocation = reader.getAttributeValue(null, "scdlLocation");
        String scdlResource = reader.getAttributeValue(null, "scdlResource");
        LoaderUtil.skipToEndElement(reader);

        ClassLoader cl = context.getClassLoader();
        URI contributionUri = context.getContributionUri();
        URL url;
        if (scdlLocation != null) {
            try {
                url = new URL(context.getSourceBase(), scdlLocation);
                return loadFromSideFile(name, cl, contributionUri, url, reader, context);
            } catch (MalformedURLException e) {
                MissingComposite failure = new MissingComposite("Error parsing composite url: " + scdlResource, reader);
                context.addError(failure);
                return null;
            }
        } else if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlResource, reader);
                context.addError(failure);
                return null;
            }
            return loadFromSideFile(name, cl, contributionUri, url, reader, context);
        } else {
            if (store == null) {
                // throw error as this is invalid in a bootstrap environment
                throw new UnsupportedOperationException("scdlLocation or scdlResource must be supplied as no MetaDataStore is available");
            }

            try {
                QNameSymbol symbol = new QNameSymbol(name);
                ResourceElement<QNameSymbol, Composite> element = store.resolve(contributionUri, Composite.class, symbol, context);
                if (element == null) {
                    String id = name.toString();
                    MissingComposite failure = new MissingComposite("Composite file not found: " + id, reader);
                    context.addError(failure);
                    return null;
                }
                Composite composite = element.getValue();
                Include include = new Include();
                include.setName(name);
                include.setIncluded(composite);
                return include;
            } catch (StoreException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                context.addError(failure);
                return null;
            }
        }
    }

    private Include loadFromSideFile(QName name, ClassLoader cl, URI contributionUri, URL url, XMLStreamReader reader, IntrospectionContext context) {
        Include include = new Include();
        IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, cl, url);
        Composite composite;
        try {
            composite = registry.load(url, Composite.class, childContext);
        } catch (LoaderException e) {
            InvalidValue failure = new InvalidValue("Error loading include", reader);
            context.addError(failure);
            return include;
        }
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        include.setName(name);
        include.setScdlLocation(url);
        include.setIncluded(composite);
        return include;
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
