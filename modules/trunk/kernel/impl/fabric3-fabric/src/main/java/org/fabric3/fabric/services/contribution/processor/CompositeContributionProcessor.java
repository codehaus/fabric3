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

import java.net.URI;
import javax.xml.stream.XMLInputFactory;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionProcessor;

public class CompositeContributionProcessor extends ContributionProcessorExtension implements ContributionProcessor {
    private XMLInputFactory xmlFactory;
    private final LoaderRegistry registry;


    public CompositeContributionProcessor(@Reference LoaderRegistry registry) {
        super();
        this.registry = registry;
        this.xmlFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory", getClass().getClassLoader());
    }

    public String[] getContentTypes() {
        return new String[] {Constants.COMPOSITE_CONTENT_TYPE};
    }


    public void processContent(Contribution contribution, URI artifactURI) throws ContributionException {
        throw new UnsupportedOperationException();
    }

}