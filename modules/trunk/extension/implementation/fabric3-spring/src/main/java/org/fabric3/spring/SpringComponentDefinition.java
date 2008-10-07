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
package org.fabric3.spring;

import java.util.Map;

import org.fabric3.pojo.provision.PojoComponentDefinition;
import org.fabric3.scdl.ReferenceDefinition;

import org.springframework.core.io.Resource;

/**
 * Represents the physical component definition for a Spring implementation.
 *
 * @version $Rev$ $Date$
 */
public class SpringComponentDefinition extends PojoComponentDefinition {
    private Resource resource;
    private String springBeanId;
    private Map<String, ReferenceDefinition> references;

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public String getSpringBeanId() {
        return springBeanId;
    }

    public void setSpringBeanId(String springBeanId) {
        this.springBeanId = springBeanId;
    }

    public Map<String, ReferenceDefinition> getReferences() {
        return references;
    }

    public void setReferences(Map<String, ReferenceDefinition> references) {
        this.references = references;
    }

}
