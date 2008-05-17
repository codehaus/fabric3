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

package org.fabric3.tests.function.annotation.scope;

import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

public class ScopeTest extends TestCase {
    
    private ConversationalService annotatedConversationalService;
    private ConversationalService conversationalService;
    
    private StatelessService annotatedStatelessService;
    private StatelessService statelessService;    
    
    private CompositeService compositeServiceOne;
    private CompositeService compositeServiceTwo;
    
    private CompositeService annotatedCompositeServiceOne;
    private CompositeService annotatedCompositeServiceTwo;

    
    public void testAnnotatedCompositeScope() throws Exception {
        assertEquals("Unexpected initial value", 0, annotatedCompositeServiceOne.getValue());
        assertEquals("Unexpected initial value", 0, annotatedCompositeServiceTwo.getValue());
        
        annotatedCompositeServiceOne.incrementValue();
        assertEquals("Unexpected value", 1, annotatedCompositeServiceOne.getValue());
        assertEquals("Unexpected value", 1, annotatedCompositeServiceTwo.getValue());        
        
        annotatedCompositeServiceOne.incrementValue();
        assertEquals("Unexpected value", 2, annotatedCompositeServiceOne.getValue());          
        assertEquals("Unexpected value", 2, annotatedCompositeServiceTwo.getValue());        
    }
    
    public void testCompositeScope() throws Exception {
        assertEquals("Unexpected initial value", 0, compositeServiceOne.getValue());
        assertEquals("Unexpected initial value", 0, compositeServiceTwo.getValue());
        
        compositeServiceOne.incrementValue();
        assertEquals("Unexpected value", 1, compositeServiceOne.getValue());
        assertEquals("Unexpected value", 1, compositeServiceTwo.getValue());        
        
        compositeServiceOne.incrementValue();
        assertEquals("Unexpected value", 2, compositeServiceOne.getValue());          
        assertEquals("Unexpected value", 2, compositeServiceTwo.getValue());        
    }    
    
    public void testAnnotatedStatelessScope() throws Exception {
        assertEquals("Unexpected initial value", 0, annotatedStatelessService.getValue());
        
        annotatedStatelessService.incrementValue();
        assertEquals("Unexpected value", 0, annotatedStatelessService.getValue());
        
        annotatedStatelessService.incrementValue();
        assertEquals("Unexpected value", 0, annotatedStatelessService.getValue());        
    }
    
    public void testStatelessScope() throws Exception {
        assertEquals("Unexpected initial value", 0, statelessService.getValue());
        
        statelessService.incrementValue();
        assertEquals("Unexpected value", 0, statelessService.getValue());
        
        statelessService.incrementValue();
        assertEquals("Unexpected value", 0, statelessService.getValue());        
    }            
    
    public void testAnnotatedConversationalScope() throws Exception {
        assertEquals("Unexpected initial value", 0, annotatedConversationalService.getValue());
        
        annotatedConversationalService.incrementValue();
        assertEquals("Unexpected value", 1, annotatedConversationalService.getValue());
        
        annotatedConversationalService.incrementValue();
        assertEquals("Unexpected value", 2, annotatedConversationalService.getValue());
    }
    
    public void testConversationalScope() throws Exception {
        assertEquals("Unexpected initial value", 0, conversationalService.getValue());
        
        conversationalService.incrementValue();
        assertEquals("Unexpected value", 1, conversationalService.getValue());
        
        conversationalService.incrementValue();
        assertEquals("Unexpected value", 2, conversationalService.getValue());        
    }    

    @Reference
    public void setAnnotatedConversationalService(ConversationalService annotatedConversationalService) {
        this.annotatedConversationalService = annotatedConversationalService;
    }

    @Reference
    public void setConversationalService(ConversationalService conversationalService) {
        this.conversationalService = conversationalService;
    }

    @Reference
    public void setAnnotatedStatelessService(StatelessService annotatedStatelessService) {
        this.annotatedStatelessService = annotatedStatelessService;
    }

    @Reference
    public void setStatelessService(StatelessService statelessService) {
        this.statelessService = statelessService;
    }

    @Reference
    public void setCompositeServiceOne(CompositeService compositeServiceOne) {
        this.compositeServiceOne = compositeServiceOne;
    }

    @Reference
    public void setCompositeServiceTwo(CompositeService compositeServiceTwo) {
        this.compositeServiceTwo = compositeServiceTwo;
    }

    @Reference
    public void setAnnotatedCompositeServiceOne(CompositeService annotatedCompositeServiceOne) {
        this.annotatedCompositeServiceOne = annotatedCompositeServiceOne;
    }

    @Reference
    public void setAnnotatedCompositeServiceTwo(CompositeService annotatedCompositeServiceTwo) {
        this.annotatedCompositeServiceTwo = annotatedCompositeServiceTwo;
    }
    
    
    
    
}
