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
*/
package org.fabric3.contribution.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Constants;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceElementNotFoundException;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Introspects a composite SCDL file in a contribution and produces a Composite type. This implementation assumes the CCL has all necessary artifacts
 * to perform introspection on its classpath.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeResourceProcessor implements ResourceProcessor {
    private Loader loader;
    private final XMLInputFactory xmlFactory;

    public CompositeResourceProcessor(@Reference ProcessorRegistry processorRegistry,
                                      @Reference Loader loader,
                                      @Reference XMLFactory xmlFactory) {
        processorRegistry.register(this);
        this.loader = loader;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    public String getContentType() {
        return Constants.COMPOSITE_CONTENT_TYPE;
    }

    public void index(Contribution contribution, URL url, IntrospectionContext context) throws InstallException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                context.addError(new MissingAttribute("Composite name not specified", reader));
                return;
            }
            Resource resource = new Resource(url, Constants.COMPOSITE_CONTENT_TYPE);
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            QName compositeName = new QName(targetNamespace, name);
            QNameSymbol symbol = new QNameSymbol(compositeName);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
            resource.addResourceElement(element);
            contribution.addResource(resource);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public void process(Resource resource, IntrospectionContext context) throws InstallException {
        URL url = resource.getUrl();
        ClassLoader classLoader = context.getClassLoader();
        URI contributionUri = context.getContributionUri();
        IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, classLoader, url);
        Composite composite;
        try {
            // check to see if the resoruce has already been evaluated
            composite = loader.load(url, Composite.class, childContext);
        } catch (LoaderException e) {
            throw new InstallException(e);
        }
        boolean found = false;
        for (ResourceElement element : resource.getResourceElements()) {
            if (element.getSymbol().getKey().equals(composite.getName())) {
                element.setValue(composite);
                found = true;
                break;
            }
        }
        if (!found) {
            String identifier = composite.getName().toString();
            throw new ResourceElementNotFoundException("Resource element not found: " + identifier, identifier);
        }
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        resource.setProcessed(true);

    }


}
