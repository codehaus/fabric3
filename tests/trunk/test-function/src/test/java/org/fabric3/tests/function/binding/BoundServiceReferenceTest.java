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
package org.fabric3.tests.function.binding;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.HelloService;

/**
 * Verifies a service and reference explictly bound in respective component definitions (as opposed to through
 * promotion)are handled properly.
 *
 * @version $Rev$ $Date$
 */
public class BoundServiceReferenceTest extends TestCase {
	
    @Reference protected HelloService helloService;
    @Reference protected List<HelloService> listOfReferences;
    @Reference protected Map<String, HelloService> mapOfReferences;

    public void testReferenceIsBound() {
        assertEquals("hello", helloService.send("hello"));
        assertEquals(2, listOfReferences.size());
        for (HelloService helloService : listOfReferences) {
            assertEquals("hello", helloService.send("hello"));
        }
    }

    public void testListOfReferenceIsBound() {
        assertEquals(2, listOfReferences.size());
        for (HelloService helloService : listOfReferences) {
            assertEquals("hello", helloService.send("hello"));
        }
    }

    public void testMapOfReferenceIsBound() {
        assertEquals(2, mapOfReferences.size());
        assertEquals("hello", mapOfReferences.get("ONE").send("hello"));
        assertEquals("hello", mapOfReferences.get("TWO").send("hello"));
    }
}
