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
package org.fabric3.fabric.services.instancefactory;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.instancefactory.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.pojo.instancefactory.MemberSite;
import org.fabric3.pojo.instancefactory.Signature;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * @version $Rev$ $Date$
 */
public class BuildHelperImpl implements InstanceFactoryBuildHelper {
    private final ClassLoaderRegistry classLoaderRegistry;

    public BuildHelperImpl(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public Class<?> loadClass(ClassLoader cl, String name) throws ClassNotFoundException {
        return classLoaderRegistry.loadClass(cl, name);
    }

    public <T> Constructor<T> getConstructor(Class<T> implClass, List<String> argNames)
            throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader cl = implClass.getClassLoader();
        Class[] ctrArgs = new Class[argNames.size()];
        for (int i = 0; i < ctrArgs.length; i++) {
            ctrArgs[i] = loadClass(cl, argNames.get(i));
        }

        Constructor<T> ctr = implClass.getDeclaredConstructor(ctrArgs);
        ctr.setAccessible(true);
        return ctr;
    }

    public Method getMethod(Class<?> implClass, Signature signature)
            throws NoSuchMethodException, ClassNotFoundException {
        return signature == null ? null : signature.getMethod(implClass);
    }

    public Map<ValueSource, Member> getInjectionSites(Class implClass, List<InjectionSiteMapping> mappings)
            throws NoSuchFieldException, NoSuchMethodException, InstanceFactoryBuilderException {
        Map<ValueSource, Member> injectionSites = new HashMap<ValueSource, Member>();
        for (InjectionSiteMapping injectionSite : mappings) {

            ValueSource source = injectionSite.getSource();
            MemberSite memberSite = injectionSite.getSite();
            ElementType elementType = memberSite.getElementType();
            String name = memberSite.getName();

            Member member = null;
            if (elementType == ElementType.FIELD) {
                member = implClass.getDeclaredField(name);
            } else if (elementType == ElementType.METHOD) {
                Signature signature = memberSite.getSignature();
                try {
                    member = signature.getMethod(implClass);
                } catch(ClassNotFoundException cnfe) {
                    throw new InstanceFactoryBuilderException(cnfe);
                }
                                
            }
            if (member == null) {
                throw new UnknownInjectionSiteException(name);
            }
            injectionSites.put(source, member);
        }
        return injectionSites;
    }
}
