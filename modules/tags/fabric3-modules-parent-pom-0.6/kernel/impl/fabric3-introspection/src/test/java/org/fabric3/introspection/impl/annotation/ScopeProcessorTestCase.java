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

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Scope;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;

@SuppressWarnings("unchecked")
public class ScopeProcessorTestCase extends TestCase {

    public void testInvalidScope() throws Exception {

        ScopeAnnotated componentToProcess = new ScopeAnnotated();
        Scope annotation = componentToProcess.getClass().getAnnotation(Scope.class);
        ScopeProcessor<Implementation<? extends InjectingComponentType>> processor =
                new ScopeProcessor<Implementation<? extends InjectingComponentType>>();
        IntrospectionContext context = new DefaultIntrospectionContext((URI) null, null, null);
        processor.visitType(annotation, ScopeAnnotated.class, new TestImplementation(), context);
        assertTrue(context.getErrors().get(0) instanceof InvalidScope);
    }

    @Scope("ILLEGAL")
    public static class ScopeAnnotated {
    }


    public static class TestImplementation extends Implementation {
        private static final long serialVersionUID = 2759280710238779821L;

        public QName getType() {
            return null;
        }
    }

}