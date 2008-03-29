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
package org.fabric3.transform.xml;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class Node2StringTest extends TestCase {

    public void testTransform() throws Exception {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><policy xmlns=\"http://www.fabric3.org\">Test data</policy>";
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element el = doc.createElementNS("http://www.fabric3.org", "policy");
        el.appendChild(doc.createTextNode("Test data"));
        
        String output = new Node2String().transform(el, null);
        assertEquals(expected, output);
    }

}
