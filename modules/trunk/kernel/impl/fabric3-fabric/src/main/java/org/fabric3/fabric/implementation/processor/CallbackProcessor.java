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
package org.fabric3.fabric.implementation.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.CallbackDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * TODO: Verify the injected callback sites have a service contract that maps to a callback service contract associated with a service the
 * implementation provides. This will need to use the contract processor to introspect the service contract of the injection site and match it against
 * a callback contract. This may need to be done after heuristics are run.
 *
 * @version $Rev$ $Date$
 * @deprecated this class should be replaced by the new introspection framework
 */
public class CallbackProcessor extends ImplementationProcessorExtension {
    private ContractProcessor contractProcessor;

    public CallbackProcessor(@Reference ContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        Callback annotation = method.getAnnotation(Callback.class);
        if (annotation == null) {
            return;
        }
        String name = method.getName();
        if (method.getParameterTypes().length != 1) {
            throw new IllegalCallbackException("Method must have one parameter [" + name + "]", name);
        }
        try {
            ServiceContract<?> contract = contractProcessor.introspect(context.getTypeMapping(), method.getParameterTypes()[0]);
            InjectionSite site = new MethodInjectionSite(method, 0);
            CallbackDefinition definition = new CallbackDefinition(name, contract);
            type.add(definition, site);
        } catch (InvalidServiceContractException e) {
            throw new ProcessingException(e);
        }
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        Callback annotation = field.getAnnotation(Callback.class);
        if (annotation == null) {
            return;
        }
        try {
            ServiceContract<?> contract = contractProcessor.introspect(context.getTypeMapping(), field.getType());
            CallbackDefinition definition = new CallbackDefinition(field.getName(), contract);
            InjectionSite site = new FieldInjectionSite(field);
            type.add(definition, site);
        } catch (InvalidServiceContractException e) {
            throw new ProcessingException(e);
        }
    }

}