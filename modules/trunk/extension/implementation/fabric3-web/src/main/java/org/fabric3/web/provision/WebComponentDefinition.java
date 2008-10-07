/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.web.provision;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.scdl.InjectionSite;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * @version $Rev: 2803 $ $Date: 2008-02-17 05:57:55 -0800 (Sun, 17 Feb 2008) $
 */
public class WebComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = 2871569095506575868L;
    private URI classLoaderId;
    private URI contributionUri;
    private String contextUrl;
    // map of resource id to injection site name/InjectionSite pair
    private Map<String, Map<String, InjectionSite>> injectionSiteMappings = new HashMap<String, Map<String, InjectionSite>>();
    private final Map<String, Document> propertyValues = new HashMap<String, Document>();

    /**
     * Gets the classloader id.
     *
     * @return Classloader id.
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * Set the classloader id.
     *
     * @param classLoaderId Classloader id.
     */
    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

    public Map<String, Map<String, InjectionSite>> getInjectionSiteMappings() {
        return injectionSiteMappings;
    }

    public void setInjectionMappings(Map<String, Map<String, InjectionSite>> mappings) {
        injectionSiteMappings = mappings;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(String contextUrl) {
        this.contextUrl = contextUrl;
    }

    public Map<String, Document> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValue(String name, Document value) {
        propertyValues.put(name, value);
    }

}
