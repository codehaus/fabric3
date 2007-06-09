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
package org.fabric3.fabric.component.instancefactory.impl;

import java.beans.IntrospectionException;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;
import org.fabric3.pojo.reflection.definition.ReflectiveInstanceFactoryDefinition;
import org.fabric3.pojo.reflection.definition.InjectionSiteMapping;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderException;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.pojo.reflection.definition.MemberSite;
import org.fabric3.pojo.reflection.definition.Signature;

/**
 * Builds a reflection-based instance factory provider.
 *
 * @version $Date$ $Revision$
 */
@EagerInit
public class ReflectiveIFProviderBuilder<T> extends
        AbstractIFProviderBuilder<ReflectiveInstanceFactoryProvider<T>, ReflectiveInstanceFactoryDefinition> {

    @Override
    protected Class<?> getIfpdClass() {
        return ReflectiveInstanceFactoryDefinition.class;
    }

    @SuppressWarnings("unchecked")
    public ReflectiveInstanceFactoryProvider<T> build(ReflectiveInstanceFactoryDefinition ifpd, ClassLoader cl)
            throws IFProviderBuilderException {

        try {

            Class implClass = cl.loadClass(ifpd.getImplementationClass());

            Constructor ctr = getConstructor(ifpd, cl, implClass);

            Method initMethod = getCallBackMethod(implClass, ifpd.getInitMethod());

            Method destroyMethod = getCallBackMethod(implClass, ifpd.getDestroyMethod());

            List<ValueSource> ctrInjectSites = ifpd.getCdiSources();

            Map<ValueSource, Member> injectionSites = getInjectionSites(ifpd, implClass);
            return new ReflectiveInstanceFactoryProvider<T>(ctr,
                                                            ctrInjectSites,
                                                            injectionSites,
                                                            initMethod,
                                                            destroyMethod);

        } catch (ClassNotFoundException ex) {
            throw new IFProviderBuilderException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IFProviderBuilderException(ex);
        } catch (NoSuchFieldException ex) {
            throw new IFProviderBuilderException(ex);
        } catch (IntrospectionException ex) {
            throw new IFProviderBuilderException(ex);
        }
    }

    /*
     * Get injection sites.
     */
    private Map<ValueSource, Member> getInjectionSites(ReflectiveInstanceFactoryDefinition ifpd, Class implClass)
            throws NoSuchFieldException, IntrospectionException, IFProviderBuilderException {

        Map<ValueSource, Member> injectionSites = new HashMap<ValueSource, Member>();
        for (InjectionSiteMapping injectionSite : ifpd.getInjectionSites()) {

            ValueSource source = injectionSite.getSource();
            MemberSite memberSite = injectionSite.getSite();
            ElementType elementType = memberSite.getElementType();
            String name = memberSite.getName();

            Member member = null;
            if (memberSite.getElementType() == ElementType.FIELD) {
                member = implClass.getDeclaredField(name);
            } else if (elementType == ElementType.METHOD) {
                // FIXME look up directly based on signature sent in RIFPD
                Method[] methods = implClass.getMethods();
                for (Method method : methods) {
                    if (name.equals(method.getName())) {
                        member = method;
                        break;
                    }
                }
            }
            if (member == null) {
                throw new UnknownInjectionSiteException(name);
            }
            injectionSites.put(source, member);
        }
        return injectionSites;
    }

    private Method getCallBackMethod(Class<?> implClass, Signature signature)
            throws NoSuchMethodException, ClassNotFoundException {
        return signature == null ? null : signature.getMethod(implClass);
    }

    /*
     * Gets the matching constructor.
     */
    private Constructor getConstructor(ReflectiveInstanceFactoryDefinition ifpd, ClassLoader cl, Class implClass)
            throws ClassNotFoundException, NoSuchMethodException {
        List<String> argNames = ifpd.getConstructorArguments();
        Class[] ctrArgs = new Class[argNames.size()];
        for (int i = 0; i < ctrArgs.length; i++) {
            ctrArgs[i] = getArgType(argNames.get(i), cl);
        }
        return implClass.getDeclaredConstructor(ctrArgs);
    }

    // xcv test this
    private Class<?> getArgType(String name, ClassLoader cl) throws ClassNotFoundException {
        if ("int".equals(name)) {
            return Integer.TYPE;
        } else if ("short".equals(name)) {
            return Short.TYPE;
        } else if ("byte".equals(name)) {
            return Byte.TYPE;
        } else if ("char".equals(name)) {
            return Character.TYPE;
        } else if ("long".equals(name)) {
            return Long.TYPE;
        } else if ("float".equals(name)) {
            return Float.TYPE;
        } else if ("double".equals(name)) {
            return Double.TYPE;
        } else if ("boolean".equals(name)) {
            return Boolean.TYPE;
        }
        return cl.loadClass(name);
    }


}
