  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.binding.ws.axis2.runtime.jaxb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebFault;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.axis2.provision.jaxb.JaxbInterceptorDefinition;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision$ $Date$
 */
public class JaxbInterceptorBuilder implements InterceptorBuilder<JaxbInterceptorDefinition> {

    private ClassLoaderRegistry classLoaderRegistry;

    public JaxbInterceptorBuilder(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public Interceptor build(JaxbInterceptorDefinition definition) throws BuilderException {

        URI classLoaderId = definition.getWireClassLoaderId();

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
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // The JAXBContext searches the TCCL for the JAXB-RI. Set the TCCL to the Axis classloader (which loaded this class), as it has 
            // visibility to the JAXB RI.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return JAXBContext.newInstance(classes);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
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
