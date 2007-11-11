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
package org.fabric3.runtime.development.host;

import java.util.Map;

/**
 * Implementations cache mock definitions registered with the domain.
 *
 * @version $Rev$ $Date$
 */
public interface MockObjectCache {

    /**
     * Add the mock to the cache
     *
     * @param name       the name of the reference being mocked
     * @param definition the data structure containing the mock and the interface it implements
     */
    void putMockDefinition(String name, MockDefinition<?> definition);

    /**
     * Returns a mock definition for the given reference name.
     *
     * @param name the name of the mock
     * @return the mock definition
     */
    MockDefinition getMockDefinition(String name);

    /**
     * Returns the mock registered definitions.
     *
     * @return the mock registered definitions.
     */
    Map<String, MockDefinition<?>> getMockDefinitions();

}
