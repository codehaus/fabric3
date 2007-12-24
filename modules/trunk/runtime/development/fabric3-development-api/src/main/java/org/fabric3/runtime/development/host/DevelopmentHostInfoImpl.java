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
package org.fabric3.runtime.development.host;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.runtime.AbstractHostInfo;

/**
 * @version $Rev$ $Date$
 */
public class DevelopmentHostInfoImpl extends AbstractHostInfo implements DevelopmentHostInfo {
    private File extensionsDir;
    private URL intentsLocation;

    public DevelopmentHostInfoImpl(final URI domain, final URL baseUrl, File extensionsDir, URL intentsLocation) {
        super(domain, baseUrl, true, URI.create("localhost://DevelopmentRuntime"));
        this.extensionsDir = extensionsDir;
        this.intentsLocation = intentsLocation;
    }

    public File getExtensionsDirectory() {
        return extensionsDir;
    }

    public URL getIntentsLocation() {
        return intentsLocation;
    }

}
