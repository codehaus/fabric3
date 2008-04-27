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
package org.fabric3.jpa.runtime;

import java.net.URI;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.provision.PersistenceUnitWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the target side of entity manager factories.
 *
 * @version $Revision$ $Date$
 */
public class PersistenceUnitWireAttacher implements TargetWireAttacher<PersistenceUnitWireTargetDefinition> {
    private final EmfBuilder emfBuilder;
    private final ClassLoaderRegistry classLoaderRegistry;

    /**
     * Injects the dependencies.
     *
     * @param classLoaderRegistry Classloader registry.
     * @param emfBuilder          Entity manager factory builder.
     */
    public PersistenceUnitWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference EmfBuilder emfBuilder) {
        this.emfBuilder = emfBuilder;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, PersistenceUnitWireTargetDefinition target,
                               Wire wire) throws WiringException {

        String unitName = target.getUnitName();
        URI classLoaderUri = target.getClassLoaderUri();

        ClassLoader appCl = classLoaderRegistry.getClassLoader(classLoaderUri);
        ClassLoader systemCl = getClass().getClassLoader();
        ClassLoader hostCl = systemCl.getParent();

        MultiParentClassLoader tccl = new MultiParentClassLoader(URI.create("JPA"), hostCl);
        tccl.addParent(appCl);
        tccl.addParent(systemCl);

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {

            Thread.currentThread().setContextClassLoader(tccl);

            final EntityManagerFactory entityManagerFactory = emfBuilder.build(unitName, tccl);

            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {

                final PhysicalOperationDefinition op = entry.getKey();
                final String opName = op.getName();
                InvocationChain chain = entry.getValue();

                chain.addInterceptor(new EmfInterceptor(opName, entityManagerFactory));

            }

        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

    /*
     * Target interceptor for entity manager factory.
     */
    private class EmfInterceptor implements Interceptor {

        private Interceptor next;
        private String opName;
        private EntityManagerFactory entityManagerFactory;

        private EmfInterceptor(String opName, EntityManagerFactory entityManagerFactory) {
            this.opName = opName;
            this.entityManagerFactory = entityManagerFactory;
        }

        public Interceptor getNext() {
            return next;
        }

        public Message invoke(Message msg) {

            Object ret = null;

            // TODO cater for the overloaded createEntityManager method
            if ("createEntityManager".equals(opName)) {
                ret = entityManagerFactory.createEntityManager();
            } else if ("close".equals(opName)) {
                entityManagerFactory.close();
            } else if ("isOpen".equals(opName)) {
                ret = entityManagerFactory.isOpen();
            }

            Message result = new MessageImpl();
            result.setBody(ret);

            return result;

        }

        public void setNext(Interceptor next) {
            this.next = next;
        }

    }

    public ObjectFactory<?> createObjectFactory(PersistenceUnitWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}
