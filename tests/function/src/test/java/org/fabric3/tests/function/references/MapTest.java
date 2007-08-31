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
package org.fabric3.tests.function.references;

import java.util.Map;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.IdentityService;

/**
 * @version $Rev$ $Date$
 */
public class MapTest extends TestCase {

    private Map<String, IdentityService> setter;

    @Reference
    public void setSetter(Map<String, IdentityService> setter) {
        this.setter = setter;
    }

    public void testSetter() {
        checkMap(setter);
    }

    private void checkMap(Map<String, IdentityService> map) {
        assertEquals(3, map.size());
        assertEquals("map.one", map.get("one").getIdentity());
        assertEquals("map.two", map.get("two").getIdentity());
        assertEquals("map.three", map.get("three").getIdentity());
    }
}
