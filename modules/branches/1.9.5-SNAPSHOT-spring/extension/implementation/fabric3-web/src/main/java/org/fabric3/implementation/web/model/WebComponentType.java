/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.web.model;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.model.type.component.ComponentType;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectionSite;

/**
 * A component type representing a web component.
 *
 * @version $Rev$ $Date$
 */
public class WebComponentType extends ComponentType {
    private static final long serialVersionUID = 9213093177241637932L;
    private final Map<String, Map<InjectionSite, Injectable>> sites = new HashMap<String, Map<InjectionSite, Injectable>>();

    /**
     * Returns a mapping from artifact id (e.g. servlet or filter class name, servlet context, session context) to injection site/injectable attribute
     * pair
     *
     * @return the mapping
     */
    public Map<String, Map<InjectionSite, Injectable>> getInjectionSites() {
        return sites;
    }

    /**
     * Sets a mapping from artifact id to injection site/injectable attribute pair.
     *
     * @param artifactId the artifact id
     * @param site       the injection site
     * @param attribute  the injectable attribute
     */
    public void addMapping(String artifactId, InjectionSite site, Injectable attribute) {
        Map<InjectionSite, Injectable> mapping = sites.get(artifactId);
        if (mapping == null) {
            mapping = new HashMap<InjectionSite, Injectable>();
            sites.put(artifactId, mapping);
        }
        mapping.put(site, attribute);
    }
}
