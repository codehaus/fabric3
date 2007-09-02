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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.IdentityService;

/**
 * @version $Rev$ $Date$
 */
public class MultiplicityTest extends TestCase {

    @Reference
    public List<IdentityService> listField;
    
    private List<IdentityService> listSetter;

    @Reference
    public void setListSetter(List<IdentityService> listSetter) {
        this.listSetter = listSetter;
    }

    public void testListSetter() {
        checkContent(listSetter);
    }

    public void testListField() {
        checkContent(listField);
    }

    private static final Set<String> IDS;

    static {
        IDS = new HashSet<String>(3);
        IDS.add("map.one");
        IDS.add("map.two");
        IDS.add("map.three");
    }

    private void checkContent(Collection<IdentityService> refs) {
        assertEquals(3, refs.size());
        for (IdentityService ref : refs) {
            // Sets dont guarantee insert order
            assertTrue(IDS.contains(ref.getIdentity()));
        }
    }
}
