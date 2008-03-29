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

import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Interface that indicates that a SCA definition supports references to intent or policySet definitions.
 *
 * @version $Rev$ $Date$
 */
public interface PolicyAware {
    /**
     * Returns the intents this definition references.
     *
     * @return the intents this definition references
     */
    Set<QName> getIntents();

    /**
     * Returns the policySets this definition references.
     *
     * @return the policySets this definition references
     */
    Set<QName> getPolicySets();

    /**
     * Sets the intents this definition references.
     *
     * @param intents the intents this definition references
     */
    void setIntents(Set<QName> intents);

    /**
     * Returns the policySets this definition references.
     *
     * @param policySets the policySets this definition references
     */
    void setPolicySets(Set<QName> policySets);
}
