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
package org.fabric3.binding.ws.axis2.databinding;

import java.net.URI;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebFault;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JaxbInterceptorBuilder implements InterceptorBuilder<JaxbInterceptorDefinition, JaxbInterceptor> {

    private InterceptorBuilderRegistry interceptorBuilderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    public JaxbInterceptorBuilder(@Reference InterceptorBuilderRegistry interceptorBuilderRegistry,
                                  @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.interceptorBuilderRegistry = interceptorBuilderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        interceptorBuilderRegistry.register(JaxbInterceptorDefinition.class, this);
    }

    public JaxbInterceptor build(JaxbInterceptorDefinition definition) throws BuilderException {

        URI classLoaderId = definition.getClassLoaderId();

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);

        try {
            Set<String> classNames = definition.getClassNames();
            Set<String> faultNames = definition.getFaultNames();
            Map<Class<?>, Constructor<?>> faultMapping = getFaultMapping(classLoader, faultNames);

            JAXBContext context = getJAXBContext(classLoader, classNames);
            return new JaxbInterceptor(classLoader, context, definition.isService(), faultMapping);

        } catch (NoSuchMethodException e) {
            throw new JaxbBuilderException(e);
        } catch (ClassNotFoundException e) {
            throw new JaxbBuilderException(e);
        } catch (JAXBException e) {
            throw new JaxbBuilderException(e);
        }

    }

    private JAXBContext getJAXBContext(ClassLoader classLoader, Set<String> classNames) throws JAXBException, ClassNotFoundException {
        Class<?>[] classes = new Class<?>[classNames.size()];
        int i = 0;
        for (String className : classNames) {
            classes[i++] = classLoaderRegistry.loadClass(classLoader, className);
        }
        return JAXBContext.newInstance(classes);
    }

    private Map<Class<?>, Constructor<?>> getFaultMapping(ClassLoader classLoader, Set<String> faultNames)
            throws ClassNotFoundException, NoSuchMethodException {
        Map<Class<?>, Constructor<?>> mapping = new HashMap<Class<?>, Constructor<?>>(faultNames.size());
        for (String faultName : faultNames) {
            Class<?> clazz = classLoaderRegistry.loadClass(classLoader, faultName);
            WebFault fault = clazz.getAnnotation(WebFault.class);
            if (fault == null) {
                // FIXME throw someting
                throw new RuntimeException();
            }
            Method getFaultInfo = clazz.getMethod("getFaultInfo");
            Class<?> faultType = getFaultInfo.getReturnType();
            Constructor<?> constructor = clazz.getConstructor(String.class, faultType);
            mapping.put(faultType, constructor);
        }
        return mapping;
    }
}
