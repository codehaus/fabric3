/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.introspection.impl.policy;

import java.lang.annotation.Annotation;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.annotation.security.RolesAllowed;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.PolicyAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class DefaultOperationPolicyIntrospectorTestCase extends TestCase {

    public void testOperationIntrospection() throws Exception {
        ContractProcessor contractProcessor = new DefaultContractProcessor(new DefaultIntrospectionHelper());

        PolicyAnnotationProcessor processor = EasyMock.createNiceMock(PolicyAnnotationProcessor.class);
        processor.process(EasyMock.isA(Annotation.class), EasyMock.isA(Operation.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall();
        EasyMock.replay(processor);

        DefaultOperationPolicyIntrospector introspector = new DefaultOperationPolicyIntrospector(processor);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        ServiceContract<?> contract = contractProcessor.introspect(new TypeMapping(), TestService.class, context);
        introspector.introspectPolicyOnOperations(contract, TestServiceImpl.class, context);
        EasyMock.verify(processor);
    }

    private static interface TestService {
        void test();

        void test(String string);
    }

    private static class TestServiceImpl implements TestService {

        public void test() {

        }

        @RolesAllowed("role1")
        public void test(String string) {

        }
    }
}
