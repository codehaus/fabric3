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
package org.fabric3.test.list;

import java.util.List;

import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class IncrementerTest extends TestCase {
    
    private List<Incrementer> incrementers;
    
    @Reference(required=true)
    public void setIncrementers(List<Incrementer> incrementers) {
        this.incrementers = incrementers;
    }

    public void testIncrement() {
        
        int val = 0;
        int count = 0;
        for(Incrementer incrementer : incrementers) {
            count++;
            val = incrementer.increment(val);
        }
        assertEquals(count, val);
        
    }

}
