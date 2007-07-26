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
package org.fabric3.fabric.services.contenttype;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class DefaultContentTypeResolverTest extends TestCase {

    public void testGetContentType() throws Exception {
        
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        URL url = getClass().getResource("test.composite");
        
        Map<String, String> extensionMap = new HashMap<String, String>();
        extensionMap.put("composite", "text/vnd.fabric3.composite+xml");
        resolver.setExtensionMap(extensionMap);
        
        assertEquals("text/vnd.fabric3.composite+xml", resolver.getContentType(url));
    }

}
