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

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.ValueSource;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
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

    public <T> Constructor<T> getConstructor(Class<T> implClass, Signature signature)
            throws ClassNotFoundException, NoSuchMethodException {

        @SuppressWarnings("unchecked")
        Constructor<T> ctr = signature.getConstructor(implClass);
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
        for (InjectionSiteMapping mapping : mappings) {

            ValueSource source = mapping.getSource();
            InjectionSite injectionSite = mapping.getSite();
            Member member;
            switch (injectionSite.getElementType()) {
            case METHOD:
                MethodInjectionSite methodSite = (MethodInjectionSite) injectionSite;
                try {
                    member = methodSite.getSignature().getMethod(implClass);
                } catch (ClassNotFoundException e) {
                    throw new InstanceFactoryBuilderException(e);
                }
                break;
            case FIELD:
                member = implClass.getDeclaredField(((FieldInjectionSite)injectionSite).getName());
                break;
            default:
                throw new AssertionError();
            }
            injectionSites.put(source, member);
        }
        return injectionSites;
    }
}
