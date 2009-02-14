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
package org.fabric3.tests.function.references;

import java.util.Map;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.tests.function.common.IdentityService;

/**
 * @version $Rev$ $Date$
 */
public class MapTest extends TestCase {

    @Reference
    public Map<String, IdentityService> field;

    private final Map<String, IdentityService> constructor;
    private Map<String, IdentityService> setter;

    public MapTest(@Reference(name = "constructor") Map<String, IdentityService> constructor) {
        this.constructor = constructor;
    }

    @Reference
    public void setSetter(Map<String, IdentityService> setter) {
        this.setter = setter;
    }

    public void testConstructor() {
        checkMap(constructor);
    }

    public void testSetter() {
        checkMap(setter);
    }

    public void testField() {
        checkMap(field);
    }

    private void checkMap(Map<String, IdentityService> map) {
        assertEquals(3, map.size());
        assertEquals("map.one", map.get("one").getIdentity());
        assertEquals("map.two", map.get("two").getIdentity());
        assertEquals("map.three", map.get("three").getIdentity());
    }
}
