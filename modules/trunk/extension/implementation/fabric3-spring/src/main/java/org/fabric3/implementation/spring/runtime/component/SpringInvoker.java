/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.implementation.spring.runtime.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Invokes a target Spring bean. When the bean is invoked, the thread context classloader will be set to the classloader for the contribution that
 * contains the application context.
 *
 * @version $Rev$ $Date$
 */
public class SpringInvoker implements Interceptor {
    private String beanName;
    private SpringComponent component;
    private Object beanProxy;
    private Method beanMethod;
    private ClassLoader targetTCCLClassLoader;

    public SpringInvoker(String beanName, Method beanMethod, SpringComponent component) {
        this.beanName = beanName;
        this.beanMethod = beanMethod;
        this.component = component;
        targetTCCLClassLoader = component.getClassLoader();
    }

    public Message invoke(Message msg) {
        WorkContext oldWorkContext = null;
        try {
            if (beanProxy == null) {
                beanProxy = component.getBean(beanName);
                if (beanProxy == null) {
                    throw new ServiceRuntimeException("Bean not found:" + beanName);
                }
            }
            WorkContext workContext = msg.getWorkContext();
            oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
            Object body = msg.getBody();
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                msg.setBody(beanMethod.invoke(beanProxy, (Object[]) body));
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        } catch (InvocationTargetException e) {
            msg.setBodyWithFault(e.getCause());
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        } catch (SpringBeanNotFoundException e) {
            // this should not happen at this point
            throw new InvocationRuntimeException("Bean not found: " + beanName, e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
        }
        return msg;
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }

}
