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
package org.fabric3.pojo.instancefactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.fabric3.scdl.ValueSource;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.InjectionSiteMapping;

/**
 * @version $Rev$ $Date$
 */
public interface InstanceFactoryBuildHelper {
    Class<?> loadClass(ClassLoader cl, String name) throws ClassNotFoundException;

    <T> Constructor<T> getConstructor(Class<T> implClass, List<String> argNames)
            throws ClassNotFoundException, NoSuchMethodException;

    Method getMethod(Class<?> implClass, Signature signature)
            throws NoSuchMethodException, ClassNotFoundException;

    Map<ValueSource, Member> getInjectionSites(Class implClass, List<InjectionSiteMapping> mappings)
            throws NoSuchFieldException, NoSuchMethodException, InstanceFactoryBuilderException;
}
