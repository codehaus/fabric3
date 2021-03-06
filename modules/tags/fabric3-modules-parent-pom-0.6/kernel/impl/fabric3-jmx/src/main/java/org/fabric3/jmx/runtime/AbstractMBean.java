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
