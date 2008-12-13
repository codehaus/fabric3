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
package org.fabric3.jmx.runtime;

import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.JMException;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractMBean implements DynamicMBean {
    protected final MBeanInfo mbeanInfo;

    public AbstractMBean(MBeanInfo mbeanInfo) {
        this.mbeanInfo = mbeanInfo;
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public AttributeList getAttributes(String[] strings) {
        AttributeList list = new AttributeList(strings.length);
        for (String s : strings) {
            try {
                Object value = getAttribute(s);
                list.add(new Attribute(s, value));
            } catch (JMException e) {
                // ignore exceptions which means the attribute won't be in the result
            }
        }
        return list;
    }

    public AttributeList setAttributes(AttributeList attributeList) {
        AttributeList result = new AttributeList(attributeList.size());
        for (Object o : attributeList) {
            Attribute attribute = (Attribute) o;
            try {
                setAttribute(attribute);
            } catch (JMException e) {
                // ignore exceptions which means the attribute won't be in the result
            }
        }
        return result;
    }
}
