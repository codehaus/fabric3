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
package org.fabric3.binding.hessian.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianServiceException;
import com.caucho.hessian.io.SerializerFactory;
import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * @version $Revision$ $Date$
 */
public class HessianTargetInterceptor implements Interceptor {

    /**
     * Next interceptor in the chain.
     */
    private Interceptor next;

    /**
     * Reference URL
     */
    private final URL referenceUrl;

    /**
     * Method name
     */
    private final String methodName;

    /**
     * The classloader to use to deserialize the response.
     */
    private final ClassLoader classLoader;

    /**
     * Factory for deserializers.
     */
    private final SerializerFactory serializerFactory;

    /**
     * Initializes the reference URL.
     *
     * @param referenceUrl      The reference URL.
     * @param methodName        the name of the method to invoke
     * @param classLoader       classloader to use to deserialize the response
     * @param serializerFactory the factory for Hessian serializers
     */
    public HessianTargetInterceptor(URL referenceUrl, String methodName, ClassLoader classLoader, SerializerFactory serializerFactory) {
        this.referenceUrl = referenceUrl;
        this.methodName = methodName;
        this.classLoader = classLoader;
        this.serializerFactory = serializerFactory;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message message) {
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) referenceUrl.openConnection();
        } catch (IOException e) {
            throw new ServiceUnavailableException(e);
        }
        try {
            sendRequest(con, message);
            return receiveResponse(con);
        } catch (IOException e) {
            throw new ServiceUnavailableException(e);
        } finally {
            con.disconnect();
        }
    }

    private void sendRequest(HttpURLConnection conn, Message message) throws IOException {
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "x-application/hessian");

        OutputStream os = conn.getOutputStream();
        Hessian2Output output = new Hessian2Output(os);
        output.setSerializerFactory(serializerFactory);
        output.startCall();
        // write out call frames as headers
        output.writeHeader("callFrames");
        output.writeObject(message.getWorkContext().getCallFrameStack());
        output.writeMethod(methodName);
        Object[] args = (Object[]) message.getBody();
        if (args != null) {
            for (Object arg : args) {
                output.writeObject(arg);
            }
        }
        output.completeCall();
        output.flush();

    }

    private Message receiveResponse(HttpURLConnection con) throws IOException {
        Hessian2Input input = new Hessian2Input(con.getInputStream());
        input.setSerializerFactory(serializerFactory);

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Message result = new MessageImpl();
            // reading the reply returns the response or for a fault throws the cause
            // there does not seem to be a way to separate service exceptions from transport problems
            // for now, treat all IOEXceptions as transport failures
            try {
                Object retValue = input.readReply(null);
                result.setBody(retValue);
            } catch (IOException e) {
                throw e;
            } catch (HessianServiceException e) {
                // work around for FABRICTHREE-161
                result.setBodyWithFault(workAroundThrowableSerialization(e));
            } catch (Throwable throwable) {
                result.setBodyWithFault(throwable);
            }
            return result;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    private Throwable workAroundThrowableSerialization(HessianServiceException e) {
        String text = e.getMessage();
        Class<?> type = (Class<?>) e.getDetail();
        try {
            Constructor<?> ctr = type.getConstructor(String.class);
            return (Throwable) ctr.newInstance(text);
        } catch (Exception ex) {
            throw new ServiceUnavailableException(ex);
        }
    }
}
