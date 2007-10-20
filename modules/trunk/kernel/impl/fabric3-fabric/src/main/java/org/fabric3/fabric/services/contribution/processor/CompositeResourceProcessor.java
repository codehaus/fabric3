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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.ResourceProcessor;

/**
 * Introspects a composite SCDL file in a contribution and produces a Composite type. This implementation assumes the
 * CCL has all necessary artifacts to perform introspection on its classpath.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeResourceProcessor implements ResourceProcessor {
    private LoaderRegistry loaderRegistry;
    private XMLInputFactory xmlFactory;

    public CompositeResourceProcessor(@Reference ProcessorRegistry processorRegistry,
                                      @Reference LoaderRegistry loaderRegistry,
                                      @Reference XMLInputFactory xmlFactory) {
        processorRegistry.register(this);
        this.loaderRegistry = loaderRegistry;
        this.xmlFactory = xmlFactory;
    }

    public String getContentType() {
        return Constants.COMPOSITE_CONTENT_TYPE;
    }

    public Resource process(InputStream stream) throws ContributionException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Composite composite = processComponentType(stream, loader);
            QNameSymbol symbol = new QNameSymbol(composite.getName());
            ResourceElement<QNameSymbol, Composite> element =
                    new ResourceElement<QNameSymbol, Composite>(symbol, composite);
            Resource resource = new Resource();
            resource.addResourceElement(element);
            return resource;
        } catch (IOException e) {
            throw new ContributionException(e);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (LoaderException e) {
            throw new ContributionException(e);
        }
    }

    /**
     * Loads a composite component type at the given URL
     *
     * @param stream the stream to load from
     * @param loader the classloader to load resources with
     * @return the component type
     * @throws java.io.IOException if an error occurs reading the URL stream
     * @throws javax.xml.stream.XMLStreamException
     *                             if an error occurs parsing the XML
     * @throws org.fabric3.spi.loader.LoaderException
     *                             if an error occurs processing the component type
     */
    private Composite processComponentType(InputStream stream, ClassLoader loader)
            throws IOException, XMLStreamException, LoaderException {
        XMLStreamReader reader = null;
        try {
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            LoaderContext context = new LoaderContextImpl(loader, null);
            return loaderRegistry.load(reader, Composite.class, context);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
