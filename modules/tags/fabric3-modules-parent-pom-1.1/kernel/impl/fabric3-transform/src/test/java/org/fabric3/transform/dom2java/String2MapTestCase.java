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
package org.fabric3.transform.dom2java;

import java.util.Map;

import org.fabric3.transform.dom2java.generics.map.String2MapOfString2String;
import org.w3c.dom.Node;

public class String2MapTestCase extends BaseTransformTest {

    public void testTransform() throws Exception {

        String2MapOfString2String string2MapOfString2String = new String2MapOfString2String();

        String xml = "<value><apple>yellow</apple><lime>green</lime><grape>black</grape></value>";

        Node node = getNode(xml);
        
        Map<String, String> map = string2MapOfString2String.transform(node, null);

        assertEquals(3, map.size());
        assertEquals("yellow", map.get("apple"));
        assertEquals("green", map.get("lime"));
        assertEquals("black", map.get("grape"));
    }

}
