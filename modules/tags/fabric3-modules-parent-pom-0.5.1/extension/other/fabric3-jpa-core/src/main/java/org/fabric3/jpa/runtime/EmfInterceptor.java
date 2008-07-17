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
package org.fabric3.jpa.runtime;

import javax.persistence.EntityManagerFactory;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * Target interceptor for entity manager factory.
 *
 * @version $Revision$ $Date$
 */
public class EmfInterceptor implements Interceptor {

    private Interceptor next;
    private String opName;
    private EntityManagerFactory entityManagerFactory;

    public EmfInterceptor(String opName, EntityManagerFactory entityManagerFactory) {
        this.opName = opName;
        this.entityManagerFactory = entityManagerFactory;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message msg) {

        Object ret = null;

        // TODO cater for the overloaded createEntityManager method
        if ("createEntityManager".equals(opName)) {
            ret = entityManagerFactory.createEntityManager();
        } else if ("close".equals(opName)) {
            entityManagerFactory.close();
        } else if ("isOpen".equals(opName)) {
            ret = entityManagerFactory.isOpen();
        }

        Message result = new MessageImpl();
        result.setBody(ret);

        return result;

    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}

