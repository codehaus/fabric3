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
package org.fabric3.tests.async;

import org.fabric3.tests.async.AsyncService;
import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class AsyncTest extends TestCase {
    
    private AsyncService asyncService;
    
    @Reference
    public void setAsyncService(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    public void testAsync() {
        
        asyncService.sendOneway("Message");
        
        while(System.getProperty("payload") == null) {
        }
        
        String payload = System.getProperty("payload");
        assertEquals("Message", payload);
        
    }
}
