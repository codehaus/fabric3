/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.ws.metro.runtime.wire;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.metro.runtime.core.TargetInterceptor;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Provides the infrastructure for invoking a target web service.
 */
public class MetroTargetWireAttacher implements TargetWireAttacher<MetroWireTargetDefinition> {

    @Reference
    protected ClassLoaderRegistry classLoaderRegistry;
    @Reference
    protected FeatureResolver featureResolver;

    /**
     * Attaches to the target.
     */
    public void attachToTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target, Wire wire) throws WiringException {

        try {

            WsdlElement wsdlElement = target.getWsdlElement();
            URL[] referenceUrls = target.getTargetUrls();
            String interfaze = target.getInterfaze();
            URI classLoaderId = source.getClassLoaderId();
            List<QName> requestedIntents = target.getRequestedIntents();
            List<PolicySet> requestedPolicySets = null;

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
            WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents, requestedPolicySets);

            Class<?> sei = classLoader.loadClass(interfaze);

            // Metro requires library classes to be visibile to the application classloader. If executing in an environment that supports classloader
            // isolation, dynamically update the application classloader by setting a parent to the Metro classloader.
            ClassLoader seiClassLoader = sei.getClassLoader();
            if (seiClassLoader instanceof MultiParentClassLoader) {
                MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) seiClassLoader;
                ClassLoader extensionCl = getClass().getClassLoader();
                if (!multiParentClassLoader.getParents().contains(extensionCl)) {
                    multiParentClassLoader.addParent(extensionCl);
                }
            }
            Method[] methods = sei.getDeclaredMethods();

            for (InvocationChain chain : wire.getInvocationChains()) {
                Method method = null;
                for (Method meth : methods) {
                    if (chain.getPhysicalOperation().getName().equals(meth.getName())) {
                        method = meth;
                        break;
                    }
                }
                TargetInterceptor targetInterceptor = new TargetInterceptor(wsdlElement, sei, referenceUrls, seiClassLoader, method, features);
                chain.addInterceptor(targetInterceptor);
            }

        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }

    }

    /**
     * Creates an object factory.
     */
    public ObjectFactory<?> createObjectFactory(MetroWireTargetDefinition target) throws WiringException {
        return null;
    }

    /**
     * Detach from the target.
     */
    public void detachFromTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target) throws WiringException {
    }

}
