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
package org.fabric3.spi.loader;

import java.net.URI;
import java.net.URL;

/**
 * A holder that can be used during the load process to store information that is not part of the logical assembly
 * model. This should be regarded as transient and references to this context should not be stored inside the model.
 *
 * @version $Rev$ $Date$
 */
public interface LoaderContext {

    /**
     * Returns a class loader that can be used to load application resources.
     *
     * @return a class loader that can be used to load application resources
     */
    ClassLoader getTargetClassLoader();

    /**
     * Returns the location of the SCDL definition being deployed.
     *
     * @return the location of the SCDL definition being deployed
     */
    URL getSourceBase();

    /**
     * Target namespace for this loader context.
     *
     * @return Target namespace.
     */
    String getTargetNamespace();

    /**
     * Returns the active contribution URI.
     *
     * @return the active contribution URI
     */
    URI getContributionUri();

}
