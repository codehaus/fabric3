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
package org.fabric3.jaxb.runtime.impl;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformationException;

/**
 * Transforms invocation parameters from one format to another.
 *
 * @version $Revision$ $Date$
 */
public class TransformingInterceptor<S, T> implements Interceptor {
    private Interceptor next;
    private final ClassLoader classLoader;
    private final PullTransformer<S, T> inTransformer;
    private final PullTransformer<T, S> outTransformer;

    public TransformingInterceptor(PullTransformer<S, T> inTransformer, PullTransformer<T, S> outTransformer, ClassLoader classLoader) {
        this.inTransformer = inTransformer;
        this.outTransformer = outTransformer;
        this.classLoader = classLoader;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    @SuppressWarnings({"unchecked"})
    public Message invoke(Message message) {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Object payload = message.getBody();
            if (payload != null && payload.getClass().isArray()) {
                // the payload is an array if the invocation has preceeded from a Java proxy
                Object[] params = (Object[]) message.getBody();
                if (params.length > 0) {
                    Object[] transformed = new Object[params.length];
                    for (int i = 0; i < params.length; i++) {
                        Object param = params[i];
                        transformed[i] = inTransformer.transform((S) param, null);
                        message.setBody(transformed);
                    }
                }
            } else if (payload != null) {
                // the payload is a single value if it has been serialized from a transport
                // transform the response to the target format
                Object transformed = inTransformer.transform((S) payload, null);
                message.setBody(new Object[]{transformed});
            }
            Message response = next.invoke(message);
            T result = (T) response.getBody();
            if (result != null) {
                // transform the response to the incoming format
                Object transformed = outTransformer.transform(result, null);
                if (response.isFault()) {
                    response.setBodyWithFault(transformed);
                } else {
                    response.setBody(transformed);
                }
            }
            return response;
        } catch (TransformationException e) {
            throw new InvocationRuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

}