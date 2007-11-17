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
package org.fabric3.fabric.async;

import java.lang.reflect.Method;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.Operation;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessor;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;

/**
 * Adds a non-blocking intent to an operation when the corresponding Java method is decorated with the {@link
 * org.osoa.sca.annotations.OneWay} annotation.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class NonBlockingInterfaceProcessor implements JavaInterfaceProcessor {
    public static final QName QNAME = new QName(Constants.SCA_NS, "oneWay");

    private JavaInterfaceProcessorRegistry registry;

    public NonBlockingInterfaceProcessor(@Reference JavaInterfaceProcessorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.registerProcessor(this);
    }

    public void visitInterface(Class<?> clazz, Class<?> callbackClass, JavaServiceContract contract)
            throws InvalidServiceContractException {
        // do nothing
    }

    public void visitOperation(Method method, Operation operation)
            throws InvalidServiceContractException {
        if (method.isAnnotationPresent(OneWay.class)) {
            operation.addIntent(QNAME);
        }

    }
}
