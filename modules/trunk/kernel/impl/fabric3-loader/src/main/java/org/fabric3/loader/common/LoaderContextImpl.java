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
package org.fabric3.loader.common;

import java.net.URI;
import java.net.URL;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.transform.TransformContext;

/**
 * A holder that can be used during the load process to store information that is not part of the logical assembly
 * model. This should be regarded as transient and references to this context should not be stored inside the model.
 *
 * @version $Rev$ $Date$
 */
public class LoaderContextImpl extends TransformContext implements LoaderContext {
    private String targetNamespace;
    private URI contributionUri;


    /**
     * Constructor defining properties of this context.
     *
     * @param classLoader  the classloader for loading application resources
     * @param contributionUri the active contribution URI
     * @param scdlLocation the location of the SCDL defining this composite
     */
    public LoaderContextImpl(ClassLoader classLoader, URI contributionUri, URL scdlLocation) {
        super(null, classLoader, scdlLocation, null);
        this.contributionUri = contributionUri;
    }

    /**
     * Initializes from a parent context.
     *
     * @param parentContext   Parent context.
     * @param targetNamespace Target namespace.
     */
    public LoaderContextImpl(LoaderContext parentContext, String targetNamespace) {
        super(null, parentContext.getTargetClassLoader(), parentContext.getSourceBase(), null);
        this.targetNamespace = targetNamespace;
        this.contributionUri = parentContext.getContributionUri();
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public URI getContributionUri() {
        return contributionUri;
    }
}
