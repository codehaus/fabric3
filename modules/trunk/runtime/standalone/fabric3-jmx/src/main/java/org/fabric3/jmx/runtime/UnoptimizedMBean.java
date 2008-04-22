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

import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev$ $Date$
 */
public class UnoptimizedMBean extends AbstractMBean {
    private final Map<String, Interceptor> getters;
    private final Map<String, Interceptor> setters;
    private final Map<OperationKey, Interceptor> operations;

    public UnoptimizedMBean(MBeanInfo mbeanInfo,
                        Map<String, Interceptor> getters,
                        Map<String, Interceptor> setters,
                        Map<OperationKey, Interceptor> operations) {
        super(mbeanInfo);
        this.getters = getters;
        this.setters = setters;
        this.operations = operations;
    }

    public Object getAttribute(String s) throws AttributeNotFoundException, MBeanException, ReflectionException {
        Interceptor interceptor = getters.get(s);
        if (interceptor == null) {
            throw new AttributeNotFoundException(s);
        }
        return invoke(interceptor, null);
    }

    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        Interceptor interceptor = setters.get(attribute.getName());
        if (interceptor == null) {
            throw new AttributeNotFoundException(attribute.getName());
        }
        invoke(interceptor, new Object[]{attribute.getValue()});
    }

    public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException {
        OperationKey operation = new OperationKey(s, strings);
        Interceptor interceptor = operations.get(operation);
        if (interceptor == null) {
            throw new ReflectionException(new NoSuchMethodException(operation.toString()));
        }
        return invoke(interceptor, objects);
    }

    Object invoke(Interceptor interceptor, Object[] args) throws MBeanException, ReflectionException {
        WorkContext workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame());
        Message message = new MessageImpl(args, false, workContext);
        message = interceptor.invoke(message);
        if (message.isFault()) {
            throw new MBeanException((Exception) message.getBody());
        } else {
            return message.getBody();
        }
    }
}
