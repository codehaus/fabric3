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
package org.fabric3.maven.runtime;

import java.net.URL;
import java.util.List;

import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;

/**
 * Manages booting the Maven runtime.
 *
 * @version $Rev$ $Date$
 */
public interface MavenCoordinator extends RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> {

    /**
     * The list of URLs for runtime extensions
     *
     * @param extensions the urls
     */
    void setExtensions(List<URL> extensions);

    /**
     * A URL pointing to the baseline intent definitions
     *
     * @param intentsLocation the url
     */
    void setIntentsLocation(URL intentsLocation);

    /**
     * The list of URLs for runtime user extensions
     *
     * @param extensions the urls
     */
    void setUserExtensions(List<URL> extensions);
}
