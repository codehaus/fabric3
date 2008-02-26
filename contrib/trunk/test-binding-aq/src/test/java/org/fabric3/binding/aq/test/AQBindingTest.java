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
package org.fabric3.binding.aq.test;

import java.util.Date;

import junit.framework.TestCase;

import org.fabric3.binding.aq.test.HelloRequest;
import org.fabric3.binding.aq.test.HelloResponse;
import org.fabric3.binding.aq.test.HelloService;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class AQBindingTest extends TestCase {
    
    private HelloService helloService;
        
    /**
     * Sets the HelloService
     * @param helloService
     */
    @Reference
    public void setHelloService(HelloService helloService) {
        this.helloService = helloService;
    }
    
    /**
     * test Hello 
     */
    public void testHelloRequest() {        
        final HelloRequest req = new HelloRequest("Hi how are you", new Date());        
        validate(helloService.howAreYou(req));              
    }
    
    /**
     * validate
     */
    private void validate(HelloResponse resp){
        System.out.println("HELLO RESPONSE " + resp.getResp());
        assertNotNull(resp);
        assertEquals("I am ok", resp.getResp());               
    }
}
