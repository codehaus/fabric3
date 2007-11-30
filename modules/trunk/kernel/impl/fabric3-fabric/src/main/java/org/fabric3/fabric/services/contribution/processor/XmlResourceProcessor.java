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

import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceProcessor;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * Processes an XML-based resource in a contribution, delegating to a Loader based on the root element QName
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlResourceProcessor implements ResourceProcessor {
    private LoaderRegistry loaderRegistry;
    private XMLInputFactory xmlFactory;

    public XmlResourceProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference LoaderRegistry loaderRegistry,
                                    @Reference XMLFactory xmlFactory) {
        this.loaderRegistry = loaderRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
        processorRegistry.register(this);
    }

    public String getContentType() {
        return "application/xml";
    }

    public Resource process(InputStream stream) throws ContributionException {
        XMLStreamReader reader = null;
        try {
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            
            // TODO : This is an evil hack
            if(!"definitions".equals(reader.getName().getLocalPart())) {
                return new Resource();
            }
            LoaderContext context = new LoaderContextImpl((ClassLoader) null, null);
            return loaderRegistry.load(reader, Resource.class, context);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (LoaderException e) {
            throw new ContributionException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                // TODO log error
                e.printStackTrace();
            }
        }
    }

}
