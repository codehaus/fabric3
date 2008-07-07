/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.pojo.reflection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;

import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceWrapperTestCase extends TestCase {
    private ReflectiveInstanceWrapper<Object> wrapper;
    private Object instance;
    private EventInvoker<Object> initInvoker;
    private EventInvoker<Object> destroyInvoker;
    private ClassLoader cl;

    public void testWithNoCallbacks() {
        wrapper = new ReflectiveInstanceWrapper<Object>(instance, cl, null, null, null);
        try {
            wrapper.start();
        } catch (InstanceInitializationException e) {
            fail();
        }
        try {
            wrapper.stop();
        } catch (InstanceDestructionException e) {
            fail();
        }
    }

    public void testWithStartCallback() throws ObjectCallbackException {
        initInvoker.invokeEvent(instance);
        EasyMock.replay(initInvoker);
        wrapper = new ReflectiveInstanceWrapper<Object>(instance, cl, initInvoker, null, null);
        try {
            wrapper.start();
        } catch (InstanceInitializationException e) {
            fail();
        }
        EasyMock.verify(initInvoker);
    }

    public void testWithStopCallback() throws ObjectCallbackException {
        destroyInvoker.invokeEvent(instance);
        EasyMock.replay(destroyInvoker);
        wrapper = new ReflectiveInstanceWrapper<Object>(instance, cl, null, destroyInvoker, null);
        try {
            wrapper.start();
            wrapper.stop();
        } catch (InstanceDestructionException e) {
            fail();
        } catch (InstanceInitializationException e) {
            fail();
        }
        EasyMock.verify(destroyInvoker);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        cl = getClass().getClassLoader();
        instance = new Object();
        initInvoker = createMock(EventInvoker.class);
        destroyInvoker = createMock(EventInvoker.class);
    }
}
