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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.osoa.sca.ServiceUnavailableException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

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
    private URL referenceUrl;

    /**
     * Method name
     */
    private String methodName;

    /**
     * Initializes the reference URL.
     * 
     * @param referenceUrl The reference URL.
     */
    public HessianTargetInterceptor(URL referenceUrl, String methodName) {
        this.referenceUrl = referenceUrl;
        this.methodName = methodName;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message message) {

        // TODO Cleanup resources in finally

        try {

            HttpURLConnection con = (HttpURLConnection) sendRequest(methodName, (Object[])message.getBody());

            Hessian2Input input = new Hessian2Input(con.getInputStream());
            input.setSerializerFactory(new SerializerFactory());

            Message result = new MessageImpl();
            Object retValue = input.readReply(null);
            result.setBody(retValue);
            return result;

        } catch (IOException ex) {
            throw new ServiceUnavailableException(ex);
        } catch (Throwable ex) {
            throw new ServiceUnavailableException(ex);
        }

    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    private URLConnection sendRequest(String methodName, Object[] args) throws IOException {

        URLConnection conn = null;

        conn = openConnection(referenceUrl);

        OutputStream os = conn.getOutputStream();

        Hessian2Output output = new Hessian2Output(os);
        output.setSerializerFactory(new SerializerFactory());
        output.call(methodName, args);
        output.flush();

        return conn;

    }

    /**
     * Creates the URL connection.
     */
    private URLConnection openConnection(URL url) throws IOException {
        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Type", "x-application/hessian");

        return conn;
    }

}
