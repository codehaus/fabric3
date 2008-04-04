package org.fabric3.services.codegen;

import java.lang.reflect.InvocationTargetException;

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

public interface ProxyGenerator {

    /**
     *
     * @param clazz interface to be converted to a remote interface
     * @param delegate pojo class implementing clazz interface
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */

    Object getWrapper(Class clazz, Object delegate) throws
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException,
            InstantiationException;

    /**
     *
     * @param clazz interface to be converted to a remote interface
     * @param delegate pojo class implementing clazz interface
     * @param targetNamespace JAX-WS target namespace
     * @param wsdlLocation JAX-WS wsdl location
     * @param serviceName JAX-WS service name of the web service
     * @param portName JAX-WS port name of the web service
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */

    Object getWrapper(Class clazz, Object delegate,
                           String targetNamespace,
                           String wsdlLocation,
                               String serviceName,
                               String portName) throws
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException,
            InstantiationException;
}
