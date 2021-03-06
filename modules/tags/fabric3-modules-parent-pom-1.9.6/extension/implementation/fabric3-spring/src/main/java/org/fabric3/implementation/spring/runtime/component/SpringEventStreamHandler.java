/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.spring.runtime.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.oasisopen.sca.ServiceRuntimeException;
import org.springframework.beans.BeansException;

import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Responsible for dispatching an event to a Spring bean.
 *
 * @version $Rev$ $Date$
 */
public class SpringEventStreamHandler implements EventStreamHandler {
    private String beanName;
    private String consumerName;
    private JavaType<?> type;
    private SpringComponent component;
    private ClassLoader targetTCCLClassLoader;
    private Object beanProxy;
    private Method beanMethod;

    /**
     * Constructor.
     *
     * @param beanName   the name of the bean events are dispatched to
     * @param methodName the name of the bean method events are dispatched to
     * @param type       the event type
     * @param component  the target component
     */
    public SpringEventStreamHandler(String beanName, String methodName, JavaType<?> type, SpringComponent component) {
        this.beanName = beanName;
        this.consumerName = methodName;
        this.type = type;
        this.component = component;
        targetTCCLClassLoader = component.getClassLoader();
    }

    public void handle(Object event) {
        WorkContext oldWorkContext = null;
        try {
            if (beanProxy == null) {
                beanProxy = component.getBean(beanName);
                if (beanProxy == null) {
                    throw new ServiceRuntimeException("Bean not found:" + beanName);
                }
            }
            WorkContext workContext = new WorkContext();
            oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                getConsumerMethod().invoke(beanProxy, (Object[]) event);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            Package pkg = cause.getClass().getPackage();
            if (cause instanceof RuntimeException && pkg != null && pkg.getName().startsWith("org.springframework.")) {
                // an exception generated by Spring - treat it as an infrastructure failure
                cause = new ServiceRuntimeException(cause);
            }
            // TODO Log
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        } catch (BeansException e) {
            // this should not happen at this point
            throw new InvocationRuntimeException("Error invoking bean: " + beanName, e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
        }
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException("This handler must be the last one in the handler sequence");
    }

    public EventStreamHandler getNext() {
        return null;
    }

    /**
     * Resolves the consumer method on the target bean after the Spring component has been started.
     *
     * @return the bean consumer method
     */
    private Method getConsumerMethod() {
        if (beanMethod != null) {
            return beanMethod;
        }
        Class<?> beanClass = component.getBeanClass(beanName);
        for (Method method : beanClass.getMethods()) {
            Class<?>[] params = method.getParameterTypes();
            // setters are only supported
            if (params.length == 1 && method.getName().equals(consumerName) && params[0].isAssignableFrom(type.getPhysical())) {
                beanMethod = method;
                return beanMethod;
            }
        }
        throw new ServiceRuntimeException("Could not load method with type: " + type);
    }


}