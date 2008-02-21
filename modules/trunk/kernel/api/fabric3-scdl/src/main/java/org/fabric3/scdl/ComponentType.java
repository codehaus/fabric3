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
package org.fabric3.scdl;

import javax.xml.namespace.QName;

/**
 * @version $Rev$ $Date$
 */
public class ComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property, ResourceDefinition> {
    private QName constrainingType;

    /**
     * Returns the name of the constraining type for this component type.
     *
     * @return the name of the constraining type for this component type
     */
    public QName getConstrainingType() {
        return constrainingType;
    }

    /**
     * Sets the name of the constraining type for this component type.
     *
     * @param constrainingType the name of the constraining type for this component type
     */
    public void setConstrainingType(QName constrainingType) {
        this.constrainingType = constrainingType;
    }

}
