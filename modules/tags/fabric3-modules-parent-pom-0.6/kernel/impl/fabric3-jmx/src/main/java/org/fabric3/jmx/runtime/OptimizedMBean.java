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
package org.fabric3.jmx.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.ObjectCreationException;

/**
 * @version $Rev$ $Date$
 */
public class OptimizedMBean<T> extends AbstractMBean {
    private final ObjectFactory<T> objectFactory;
    private final Map<String, Method> getters;
    private final Map<String, Method> setters;
    private final Map<OperationKey, Method> operations;

    public OptimizedMBean(ObjectFactory<T> objectFactory,
                          MBeanInfo mbeanInfo,
                          Map<String, Method> getters,
                          Map<String, Method> setters,
                          Map<OperationKey, Method> operations) {
        super(mbeanInfo);
        this.objectFactory = objectFactory;
        this.getters = getters;
        this.setters = setters;
        this.operations = operations;
    }

    public Object getAttribute(String s) throws AttributeNotFoundException, MBeanException, ReflectionException {
        Method interceptor = getters.get(s);
        if (interceptor == null) {
            throw new AttributeNotFoundException(s);
        }
        return invoke(interceptor, null);
    }

    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        Method interceptor = setters.get(attribute.getName());
        if (interceptor == null) {
            throw new AttributeNotFoundException(attribute.getName());
        }
        invoke(interceptor, new Object[]{attribute.getValue()});
    }

    public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException {
        OperationKey operation = new OperationKey(s, strings);
        Method interceptor = operations.get(operation);
        if (interceptor == null) {
            throw new ReflectionException(new NoSuchMethodException(operation.toString()));
        }
        return invoke(interceptor, objects);
    }

    Object invoke(Method interceptor, Object[] args) throws MBeanException, ReflectionException {
        WorkContext workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame());
        WorkContext oldContext = PojoWorkContextTunnel.setThreadWorkContext(workContext);
        try {
            T instance = objectFactory.getInstance();
            return interceptor.invoke(instance, args);
        } catch (ObjectCreationException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw new MBeanException((Exception) e.getCause());
            } else {
                throw new ReflectionException(e);
            }
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }
}