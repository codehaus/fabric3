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
package org.fabric3.web.introspection;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectionSite;

/**
 * A component type representing a web component.
 *
 * @version $Revision$ $Date$
 */
public class WebComponentType extends ComponentType {
    private static final long serialVersionUID = 9213093177241637932L;
    private final Map<String, Map<InjectionSite, InjectableAttribute>> sites = new HashMap<String, Map<InjectionSite, InjectableAttribute>>();

    /**
     * Returns a mapping from artifact id (e.g. servlet or filter class name, servlet context, session context) to injection site/injectable attribute
     * pair
     *
     * @return the mapping
     */
    public Map<String, Map<InjectionSite, InjectableAttribute>> getInjectionSites() {
        return sites;
    }

    /**
     * Sets a mapping from artifact id to injection site/injectable attribute pair.
     *
     * @param artifactId the artifact id
     * @param site       the injeciton site
     * @param attribute  the injectable attribute
     */
    public void addMapping(String artifactId, InjectionSite site, InjectableAttribute attribute) {
        Map<InjectionSite, InjectableAttribute> mapping = sites.get(artifactId);
        if (mapping == null) {
            mapping = new HashMap<InjectionSite, InjectableAttribute>();
            sites.put(artifactId, mapping);
        }
        mapping.put(site, attribute);
    }
}
