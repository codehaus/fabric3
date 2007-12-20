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
package org.fabric3.idea.run;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Service;

import org.fabric3.maven.runtime.MavenHostInfo;

/**
 * Default implementation of IntelliJHostInfo.
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {IntelliJHostInfo.class, MavenHostInfo.class})
public class IntelliJHostInfoImpl implements IntelliJHostInfo {
    private final URI domain;
    private final Properties hostProperties;
    private final Set<URL> dependencyUrls;
    private URL outputDirectory;
    private URL testOutputDirectory;
    private List<String> implementations;
    private List<QName> composites;

    public IntelliJHostInfoImpl(URI domain,
                                Properties hostProperties,
                                Set<URL> dependencyUrls,
                                URL outputDirectory,
                                URL testOutputDirectory,
                                List<String> implementations,
                                List<QName> composites) {
        this.domain = domain;
        this.hostProperties = hostProperties;
        this.dependencyUrls = dependencyUrls;
        this.outputDirectory = outputDirectory;
        this.testOutputDirectory = testOutputDirectory;
        this.implementations = implementations;
        this.composites = composites;
    }

    public URL getBaseURL() {
        return null;
    }

    public boolean isOnline() {
        throw new UnsupportedOperationException();
    }

    public String getProperty(String name, String defaultValue) {
        return hostProperties.getProperty(name, defaultValue);
    }

    public URI getDomain() {
        return domain;
    }

    public String getRuntimeId() {
        return "IntelliJ";
    }

    public Set<URL> getDependencyUrls() {
        return dependencyUrls;
    }

    public URL getOutputDirectory() {
        return outputDirectory;
    }

    public URL getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public List<String> getJUnitComponentImplementations() {
        return implementations;
    }

    public List<QName> getIncludedComposites() {
        return composites;
    }
}
