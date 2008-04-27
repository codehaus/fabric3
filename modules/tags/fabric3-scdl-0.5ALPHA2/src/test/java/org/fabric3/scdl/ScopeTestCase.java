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

import java.net.URI;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ScopeTestCase extends TestCase {
    public void testEquality() {
        assertEquals(Scope.COMPOSITE, Scope.COMPOSITE);
        assertEquals(Scope.COMPOSITE, new Scope<URI>("COMPOSITE", URI.class));
        Scope<?> scope = Scope.COMPOSITE;
        assertFalse(scope.equals(Scope.STATELESS));
    }
}
