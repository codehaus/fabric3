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

import java.lang.reflect.Type;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class RemotableProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Remotable, I> {

    private final ContractProcessor contractProcessor;

    public RemotableProcessor(@Reference ContractProcessor contractProcessor) {
        super(Remotable.class);
        this.contractProcessor = contractProcessor;
    }

    public void visitType(Remotable annotation, Class<?> type, I implementation, IntrospectionContext context) {
        ServiceContract<Type> serviceContract = contractProcessor.introspect(context.getTypeMapping(), type, context);
        ServiceDefinition definition = new ServiceDefinition(serviceContract.getInterfaceName(), serviceContract);
        implementation.getComponentType().add(definition);
    }
}
