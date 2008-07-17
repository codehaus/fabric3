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

import java.util.List;
import javax.xml.namespace.QName;

/**
 * @version $Rev$ $Date$
 */
public class ConstrainingType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property, ResourceDefinition> {
    private final QName name;
    private final List<QName> requires;

    /**
     * Constructor defining the constraining type name.
     *
     * @param name the qualified name of this constraining type
     * @param requires list of required intents
     */
    public ConstrainingType(QName name, List<QName> requires) {
        this.name = name;
        this.requires = requires;
    }

    /**
     * Returns the qualified name of this constraining type.
     * <p/>
     * The namespace portion of this name is the targetNamespace for other qualified names.
     *
     * @return the qualified name of this constraining type
     */
    public QName getName() {
        return name;
    }

    /**
     * Returns the intents that must be satisfied.
     *
     * @return a list of intents that must be satisified
     */
    public List<QName> getRequires() {
        return requires;
    }
}
