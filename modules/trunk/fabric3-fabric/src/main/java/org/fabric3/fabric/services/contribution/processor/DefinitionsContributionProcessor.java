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

import java.io.InputStream;
import java.net.URI;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.loader.definitions.DefinitionsLoader;
import org.fabric3.scdl.definitions.Definitions;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.services.contribution.Contribution;
import org.osoa.sca.annotations.Reference;

/**
 * Processor for file types *.definitions. This will also get delegated from
 * *.xml processors is the root element for the document is sca:definitions.
 * 
 * @version $Revision$ $Date$
 */
public class DefinitionsContributionProcessor extends ContributionProcessorExtension {

    // Definitions loader
    private DefinitionsLoader definitionsLoader;
    
    // Xml Input Factory
    private XMLInputFactory xmlInputFactory;

    /**
     * Injects the references.
     * 
     * @param definitionsLoader Injected definitions loader.
     * @param xmlInputFactory Injected XML input factory.
     */
    public DefinitionsContributionProcessor(@Reference DefinitionsLoader definitionsLoader, 
                                            @Reference XMLInputFactory xmlInputFactory) {
        this.definitionsLoader = definitionsLoader;
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
     *      java.net.URI, java.io.InputStream)
     */
    public void processContent(Contribution contribution, URI source, InputStream inputStream) throws ContributionException {

        try {
            
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);
            reader.nextTag();
            
            LoaderContext context = new LoaderContextImpl((ClassLoader) null, null);
            Definitions definitions = definitionsLoader.load(reader, context);
            
            for(PolicySet policySet : definitions.getPolicySets()) {
                contribution.addType(policySet.getName(), policySet);
            }
            
            for(Intent intent : definitions.getIntents()) {
                contribution.addType(intent.getName(), intent);
            }

        } catch (LoaderException e) {
            throw new ContributionException(e);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        }
        
    }

}
