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
package org.fabric3.binding.ejb.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.ejb.EJBException;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class EjbHomeServiceHandler implements InvocationHandler {

    private final Object serviceImpl;


    public EjbHomeServiceHandler(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if(method.getName().equals("create") && (args == null || args.length == 0))
            return serviceImpl;

        throw new EJBException("The SCA EJB Binding prohibits calls to home interface methods other than create()");

    }
}