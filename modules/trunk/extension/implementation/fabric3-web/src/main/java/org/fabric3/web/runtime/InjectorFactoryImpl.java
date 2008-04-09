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
package org.fabric3.web.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.pojo.reflection.FieldInjector;
import org.fabric3.pojo.reflection.Injector;
import org.fabric3.pojo.reflection.MethodInjector;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.web.provision.WebContextInjectionSite;

/**
 * Default implementaiton of the InjectorFactory.
 *
 * @version $Revision$ $Date$
 */
public class InjectorFactoryImpl implements InjectorFactory {

    public void createInjectorMappings(Map<String, List<Injector<?>>> injectors,
                                       Map<String, Map<String, InjectionSite>> siteMappings,
                                       Map<String, ObjectFactory<?>> factories,
                                       ClassLoader classLoader) throws InjectionCreationException {
        for (Map.Entry<String, ObjectFactory<?>> entry : factories.entrySet()) {
            String siteName = entry.getKey();
            ObjectFactory<?> factory = entry.getValue();
            Map<String, InjectionSite> artifactMapping = siteMappings.get(siteName);
            if (artifactMapping == null) {
                throw new InjectionCreationException("Injection site not found for: " + siteName);
            }
            for (Map.Entry<String, InjectionSite> siteEntry : artifactMapping.entrySet()) {
                String artifactName = siteEntry.getKey();
                InjectionSite site = siteEntry.getValue();
                List<Injector<?>> injectorList = injectors.get(artifactName);
                if (injectorList == null) {
                    injectorList = new ArrayList<Injector<?>>();
                    injectors.put(artifactName, injectorList);
                }
                Injector<?> injector;
                if (site instanceof WebContextInjectionSite) {
                    injector = createInjector(siteName, factory, (WebContextInjectionSite) site);
                } else if (site instanceof FieldInjectionSite) {
                    injector = createInjector(factory, artifactName, (FieldInjectionSite) site, classLoader);
                } else if (site instanceof MethodInjectionSite) {
                    injector = createInjector(factory, artifactName, (MethodInjectionSite) site, classLoader);
                } else {
                    throw new UnsupportedOperationException("Unsupported injection site type" + site.getElementType());
                }
                injectorList.add(injector);
            }
        }
    }

    private Injector<?> createInjector(ObjectFactory<?> factory, String artifactName, MethodInjectionSite site, ClassLoader classLoader) {
        try {
            return new MethodInjector(getMethod(site, artifactName, classLoader), factory);
        } catch (ClassNotFoundException e) {
            throw new WebComponentStartException(e);
        } catch (NoSuchMethodException e) {
            throw new WebComponentStartException(e);
        }
    }

    private Injector<?> createInjector(ObjectFactory<?> factory, String artifactName, FieldInjectionSite site, ClassLoader classLoader) {
        try {
            return new FieldInjector(getField(site, artifactName, classLoader), factory);
        } catch (NoSuchFieldException e) {
            throw new WebComponentStartException(e);
        } catch (ClassNotFoundException e) {
            throw new WebComponentStartException(e);
        }
    }

    private Injector<?> createInjector(String referenceName, ObjectFactory<?> factory, WebContextInjectionSite site) {
        if (site.getContextType() == WebContextInjectionSite.ContextType.SERVLET_CONTEXT) {
            Injector<?> injector = new ServletContextInjector();
            injector.setObectFactory(factory, referenceName);
            return injector;
        } else {
            throw new UnsupportedOperationException("Session context injection not supported");
        }
    }

    private Method getMethod(MethodInjectionSite methodSite, String implementationClass, ClassLoader classLoader)
            throws ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = classLoader.loadClass(implementationClass);
        return methodSite.getSignature().getMethod(clazz);
    }

    private Field getField(FieldInjectionSite site, String implementationClass, ClassLoader classLoader)
            throws NoSuchFieldException, ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(implementationClass);
        String name = site.getName();
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

}
