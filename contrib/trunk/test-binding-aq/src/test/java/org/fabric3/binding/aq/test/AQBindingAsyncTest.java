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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class AQBindingAsyncTest extends TestCase {

    /** Asyn Service */
    private EchoService echoService;

    /**
     * Sets the Echo Service
     * 
     * @param helloService
     */
    @Reference
    public void setEchoService(EchoService echoService) {
        this.echoService = echoService;
    }

    /**
     * test Hello
     * @throws IOException 
     */
    public void testEchoService() throws IOException {
        System.out.println("Calling Echo Service");
        /*Do Not want a LOOP */
        echoService.areYouThere("Any One There One");
        echoService.areYouThere("Any One There Two");
        echoService.areYouThere("Any One There Three");
        echoService.areYouThere("Any One There Four");
        echoService.areYouThere("Any One There Five");         
        echoService.areYouThere("Any One There Six");
        echoService.areYouThere("Any One There Seven");
        echoService.areYouThere("Any One There Eight");
        echoService.areYouThere("Any One There Nine");
        System.err.println("Press Start on JMX Console and let me know when you have finished");
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String msg = read.readLine();
        if(!msg.equals("finish")){            
           throw new RuntimeException("Message is No valid");    
        }        
    }

    /**
     * Sleeping
     * @param i
     */
    private void sleep(long val) {
        try {
            Thread.sleep(val);
        } catch (InterruptedException e) {            
            e.printStackTrace();
        }
        
    }
}
