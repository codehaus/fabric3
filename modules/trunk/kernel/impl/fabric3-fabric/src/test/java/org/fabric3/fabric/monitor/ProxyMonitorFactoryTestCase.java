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
package org.fabric3.fabric.monitor;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.util.Map;
import java.util.logging.Level;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.host.monitor.ExceptionFormatter;

/**
 * @version $Rev$ $Date$
 */
public class ProxyMonitorFactoryTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testFormatSubtype() {
        ProxyMonitorFactory factory = new TestProxyMonitorFactory();
        PrintWriter pw = EasyMock.createMock(PrintWriter.class);
        EasyMock.replay(pw);

        ExceptionFormatter<GeneralException> genericFormater = EasyMock.createMock(ExceptionFormatter.class);
        EasyMock.expect(genericFormater.canFormat(EasyMock.eq(SpecificException.class))).andReturn(false);
        EasyMock.replay(genericFormater);
        factory.register(genericFormater);

        ExceptionFormatter<SpecificException> specificFormater = EasyMock.createMock(ExceptionFormatter.class);
        EasyMock.expect(specificFormater.canFormat(EasyMock.eq(SpecificException.class))).andReturn(true);
        specificFormater.write(EasyMock.isA(PrintWriter.class), EasyMock.isA(SpecificException.class));
        EasyMock.replay(specificFormater);
        factory.register(specificFormater);


        factory.formatException(pw, new SpecificException());
        EasyMock.verify(specificFormater);
        EasyMock.verify(genericFormater);
    }

    public void testFormatGenericType() {
        ProxyMonitorFactory factory = new TestProxyMonitorFactory();
        PrintWriter pw = EasyMock.createMock(PrintWriter.class);
        EasyMock.replay(pw);

        ExceptionFormatter<SpecificException> specificFormater = EasyMock.createMock(ExceptionFormatter.class);
        EasyMock.expect(specificFormater.canFormat(EasyMock.eq(GeneralException.class))).andReturn(false);
        EasyMock.replay(specificFormater);
        factory.register(specificFormater);
        
        ExceptionFormatter<GeneralException> genericFormater = EasyMock.createMock(ExceptionFormatter.class);
        EasyMock.expect(genericFormater.canFormat(EasyMock.eq(GeneralException.class))).andReturn(true);
        genericFormater.write(EasyMock.isA(PrintWriter.class), EasyMock.isA(GeneralException.class));
        EasyMock.replay(genericFormater);
        factory.register(genericFormater);

        factory.formatException(pw, new GeneralException());
        EasyMock.verify(specificFormater);
        EasyMock.verify(genericFormater);
    }

    private class TestProxyMonitorFactory extends ProxyMonitorFactory {

        protected <T> InvocationHandler createInvocationHandler(Class<T> monitorInterface, Map<String, Level> levels) {
            return null;
        }
    }

    private static class GeneralException extends Exception {

        private static final long serialVersionUID = -3524997689380793977L;
    }

    private static class SpecificException extends GeneralException {

        private static final long serialVersionUID = -8824435099168486490L;
    }

}
