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
package org.fabric3.fabric.services.contribution.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import org.fabric3.fabric.services.contribution.IntentResourceElement;
import org.fabric3.fabric.services.contribution.PolicySetResourceElement;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.scdl.definitions.Definitions;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;

/**
 * Processor for file types *.definitions. This will also get delegated from *.xml processors is the root element for
 * the document is sca:definitions.
 *
 * @version $Revision$ $Date$
 */
public class DefinitionsContributionProcessor extends ContributionProcessorExtension {

    // Definitions loader
    private LoaderRegistry loaderRegistry;

    // Xml Input Factory
    private XMLInputFactory xmlInputFactory;

    /**
     * Injects the references.
     *
     * @param loaderRegistry  Injected definitions loader.
     * @param xmlInputFactory Injected XML input factory.
     */
    public DefinitionsContributionProcessor(@Reference(required = true)LoaderRegistry loaderRegistry,
                                            @Reference(required = true)XMLInputFactory xmlInputFactory) {
        this.loaderRegistry = loaderRegistry;
        this.xmlInputFactory = xmlInputFactory;
    }

    /**
     * @see org.fabric3.spi.services.contribution.ContributionProcessor#getContentType()
     */
    public String getContentType() {
        return Constants.DEFINITIONS_TYPE;
    }

    /**
     * @see org.fabric3.spi.services.contribution.ContributionProcessor#processContent(org.fabric3.spi.services.contribution.Contribution,
     *java.net.URI)
     */
    public void processContent(Contribution contribution, URI source) throws ContributionException {
        InputStream stream = null;
        try {
            stream = contribution.getLocation().openStream();
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(stream);
            reader.nextTag();

            LoaderContext context = new LoaderContextImpl((ClassLoader) null, null);
            Definitions definitions = loaderRegistry.load(reader, Definitions.class, context);
            Resource resource = new Resource();
            for (PolicySet policySet : definitions.getPolicySets()) {
                QNameSymbol name = new QNameSymbol(policySet.getName());
                PolicySetResourceElement element = new PolicySetResourceElement(name, policySet);
                resource.addResourceElement(element);
            }

            for (Intent intent : definitions.getIntents()) {
                QNameSymbol name = new QNameSymbol(intent.getName());
                IntentResourceElement element = new IntentResourceElement(name, intent);
                resource.addResourceElement(element);
            }
            contribution.addResource(resource);
        } catch (LoaderException e) {
            throw new ContributionException(e);
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
                //noinspection ThrowFromFinallyBlock
                throw new ContributionException(e);
            }
        }

    }

}
