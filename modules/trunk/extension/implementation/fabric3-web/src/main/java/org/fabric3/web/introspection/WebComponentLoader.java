/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.web.introspection;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.ElementLoadFailure;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;

/**
 * Loads <code><implementation.web></code> from a composite.
 *
 * @version $Rev: 3105 $ $Date: 2008-03-15 09:47:31 -0700 (Sat, 15 Mar 2008) $
 */
@EagerInit
public class WebComponentLoader implements TypeLoader<WebImplementation> {

    private LoaderRegistry registry;
    private WebImplementationIntrospector introspector;

    public WebComponentLoader(@Reference LoaderRegistry registry, @Reference WebImplementationIntrospector introspector) {
        this.registry = registry;
        this.introspector = introspector;
    }

    @Init
    public void init() {
        registry.registerLoader(WebImplementation.IMPLEMENTATION_WEB, this);
        registry.registerLoader(WebImplementation.IMPLEMENTATION_WEBAPP, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(WebImplementation.IMPLEMENTATION_WEB);
        registry.unregisterLoader(WebImplementation.IMPLEMENTATION_WEBAPP);
    }

    public WebImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        WebImplementation impl = new WebImplementation();
        introspector.introspect(impl, introspectionContext);

        try {
            ComponentType type = impl.getComponentType();
            // FIXME we should allow implementation to specify the component type;
            ComponentType componentType = loadComponentType(introspectionContext);
            for (Map.Entry<String, ReferenceDefinition> entry : componentType.getReferences().entrySet()) {
                type.add(entry.getValue());
            }
            for (Map.Entry<String, Property> entry : componentType.getProperties().entrySet()) {
                type.add(entry.getValue());
            }
        } catch (LoaderException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                // ignore since we allow component types not to be specified in the web app 
            } else {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading web.componentType", e, reader);
                introspectionContext.addError(failure);
                return null;
            }
        }
        LoaderUtil.skipToEndElement(reader);
        return impl;
    }

    private ComponentType loadComponentType(IntrospectionContext context) throws LoaderException {
        URL url;
        try {
            url = new URL(context.getSourceBase(), "web.componentType");
        } catch (MalformedURLException e) {
            // this should not happen
            throw new LoaderException(e.getMessage(), e);
        }
        IntrospectionContext childContext = new DefaultIntrospectionContext(context.getTargetClassLoader(), null, url);
        ComponentType componentType = registry.load(url, ComponentType.class, childContext);
        componentType.setScope("COMPOSITE");
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        return componentType;
    }
}
