/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
