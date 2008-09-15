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

package org.fabric3.introspection.impl.annotation;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.api.annotation.scope.Request;
import org.fabric3.api.annotation.scope.Scopes;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;

@SuppressWarnings("unchecked")
public class RequestProcessorTestCase extends TestCase {
    
    public void testScopeIdentification() throws Exception {
        
        RequestAnnotated componentToProcess = new RequestAnnotated();
        Request annotation = componentToProcess.getClass().getAnnotation(Request.class);        
        RequestProcessor<Implementation<? extends InjectingComponentType>> processor = 
                                new RequestProcessor<Implementation<? extends InjectingComponentType>>();        
        processor.visitType(annotation, componentToProcess.getClass(), componentToProcess, null);
        
        assertEquals("Unexpected scope", Scopes.REQUEST, componentToProcess.getScope());
    }
    
    @SuppressWarnings("serial")
    @Request
    public static class RequestAnnotated extends Implementation {        
        
        private String scope;
        
        public String getScope() {
            return scope;
        }

        @Override
        public AbstractComponentType getComponentType() {
            return new InjectingComponentType() {
                @Override
                public void setScope(String introspectedScope) {
                    scope = introspectedScope;
                }
            };
        };
        
        @Override
        public QName getType() {
            return null;
        }
    }
    
}
