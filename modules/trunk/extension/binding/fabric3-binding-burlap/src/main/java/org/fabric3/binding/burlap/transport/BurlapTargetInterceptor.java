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
package org.fabric3.binding.burlap.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;
import com.caucho.burlap.io.SerializerFactory;
import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;


/**
 * @version $Revision$ $Date$
 */
public class BurlapTargetInterceptor implements Interceptor {

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
    public BurlapTargetInterceptor(URL referenceUrl, String methodName) {
        this.referenceUrl = referenceUrl;
        this.methodName = methodName;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.wire.Message)
     */
    public Message invoke(Message message) {

        // TODO Cleanup resources in finally

        try {

            HttpURLConnection con = (HttpURLConnection) sendRequest(methodName, (Object[]) message.getBody());

            BurlapInput input = new BurlapInput(con.getInputStream());
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

    /**
     * @see org.fabric3.spi.wire.Interceptor#setNext(org.fabric3.spi.wire.Interceptor)
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

    private URLConnection sendRequest(String methodName, Object[] args) throws IOException {
        URLConnection conn = openConnection(referenceUrl);
        OutputStream os = conn.getOutputStream();
        BurlapOutput output = new BurlapOutput(os);
        //output.setSerializerFactory(new SerializerFactory());
        output.call(methodName, args);
        os.flush();
        //output.flush();
        return conn;

    }

    /**
     * Creates the URL connection.
     */
    private URLConnection openConnection(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "x-application/burlap");
        return conn;
    }

}
