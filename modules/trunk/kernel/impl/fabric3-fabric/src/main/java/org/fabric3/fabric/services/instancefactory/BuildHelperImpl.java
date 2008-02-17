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
import java.lang.reflect.Method;

import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.scdl.Signature;
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

}
