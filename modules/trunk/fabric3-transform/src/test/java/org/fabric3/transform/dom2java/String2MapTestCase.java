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

import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;

import junit.framework.TestCase;

public class String2MapTestCase extends TestCase {

    public void testTransform() throws Exception {

        String2Map string2Map = new String2Map();

        String xml = "<value><apple>yellow</apple><lime>green</lime><grape>black</grape></value>";

        Node node =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes()))
                .getDocumentElement();
        
        Map<String, String> map = string2Map.transform(node);

        assertEquals(3, map.size());
        assertEquals("yellow", map.get("apple"));
        assertEquals("green", map.get("lime"));
        assertEquals("black", map.get("grape"));
    }

}
