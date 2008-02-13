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

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.MemberSite;

/**
 * TODO: Verify the injected callback sites have a service contract that maps to a callback service contract associated with a service the
 * implementation provides. This will need to use the contract processor to introspect the service contract of the injection site and match it against
 * a callback contract. This may need to be done after heuristics are run.
 *
 * @version $Rev$ $Date$
 * @deprecated this class should be replaced by the new introspection framework
 */
public class CallbackProcessor extends ImplementationProcessorExtension {

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        Callback annotation = method.getAnnotation(Callback.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 1) {
            String name = method.getName();
            throw new IllegalCallbackException("Method must have one parameter [" + name + "]", name);
        }
        MemberSite site = new MemberSite(method);
        type.addCallbackSite(site);
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        Callback annotation = field.getAnnotation(Callback.class);
        if (annotation == null) {
            return;
        }
        MemberSite site = new MemberSite(field);
        type.addCallbackSite(site);
    }

}