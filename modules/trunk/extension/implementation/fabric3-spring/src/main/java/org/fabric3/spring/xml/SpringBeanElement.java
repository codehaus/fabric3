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
package org.fabric3.spring.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a <bean> element in a Spring application-context
 * - this has id and className attributes
 * - plus zero or more property elements as children
 *
 * @version
 */
public class SpringBeanElement {

    private String id;
    private String className;
    private List<SpringPropertyElement> properties = new ArrayList<SpringPropertyElement>();

    public SpringBeanElement(String id, String className) {
        this.id = id;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getId() {
        return id;
    }

    public List<SpringPropertyElement> getProperties() {
        return properties;
    }

    public void addProperty(SpringPropertyElement property) {
        properties.add(property);
    }

}
