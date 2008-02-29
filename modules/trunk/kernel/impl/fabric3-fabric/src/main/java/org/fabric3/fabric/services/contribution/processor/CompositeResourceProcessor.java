/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.services.contribution.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.loader.Loader;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.ResourceElementNotFoundException;
import org.fabric3.spi.services.contribution.ResourceProcessor;
import org.fabric3.spi.services.factories.xml.XMLFactory;

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

    public void index(Contribution contribution, URL url) throws ContributionException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            String name = reader.getAttributeValue(null, "name");
            Resource resource = new Resource(url, Constants.COMPOSITE_CONTENT_TYPE);
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            QName compositeName = new QName(targetNamespace, name);
            QNameSymbol symbol = new QNameSymbol(compositeName);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
            resource.addResourceElement(element);
            contribution.addResource(resource);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
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
    public void process(URI contributionUri, Resource resource, ClassLoader loader) throws ContributionException {
        try {
            URL url = resource.getUrl();
            Composite composite = processComponentType(url, loader, contributionUri);
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
        } catch (LoaderException e) {
            throw new ContributionException(e);
        }
    }

    /**
     * Loads a composite component type at the given URL
     *
     * @param url             the url for the component type artifact
     * @param loader          the classloader to load resources with
     * @param contributionUri the current contribution uri
     * @return the component type
     * @throws LoaderException if an error occurs processing the component type
     */
    private Composite processComponentType(URL url, ClassLoader loader, URI contributionUri) throws LoaderException {
        IntrospectionContext context = new IntrospectionContextImpl(loader, contributionUri, url);
        return this.loader.load(url, Composite.class, context);
    }


}
