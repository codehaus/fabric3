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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.fabric3.spi.wire.Wire;

/**
 * Servlet for handling the hessian service requests.
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class HessianServiceHandler extends HttpServlet {

    /**
     * Wire attached to the servlet.
     */
    private Wire wire;

    /**
     * Map of op names to operation definitions.
     */
    private Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;

    /**
     * The classloader to deserialize parameters in. Referencing the classloader directly is ok given this class must be
     * cleaned up if the target component associated with the classloader for this service is removed.
     */
    private ClassLoader classLoader;

    /**
     * Initializes the wire associated with the service.
     *
     * @param wire        Wire that connects the transport to the component.
     * @param ops         Map of op names to operation definitions.
     * @param classLoader the classloader to load parameters in
     */
    public HessianServiceHandler(Wire wire,
                                 Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops,
                                 ClassLoader classLoader) {
        this.wire = wire;
        this.ops = ops;
        this.classLoader = classLoader;
    }

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Handles the hessian requests.
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        InputStream in = request.getInputStream();

        Hessian2Input hessianInput = new Hessian2Input(in);
        hessianInput.setSerializerFactory(new SerializerFactory());

        hessianInput.readCall();
        hessianInput.readMethod();
        String methodName = hessianInput.getMethod();

        PhysicalOperationDefinition op = ops.get(methodName).getKey();
        Interceptor head = ops.get(methodName).getValue().getHeadInterceptor();

        Object[] args = new Object[op.getParameters().size()];
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // Hessian uses the TCCL to deserialize parameters
            Thread.currentThread().setContextClassLoader(classLoader);
            for (int i = 0; i < args.length; i++) {
                args[i] = hessianInput.readObject();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
        hessianInput.completeCall();

        Message input = new MessageImpl(args, false, new SimpleWorkContext(), wire);

        Message output = head.invoke(input);
        Object ret = output.getBody();

        OutputStream out = response.getOutputStream();
        Hessian2Output hessianOutput = new Hessian2Output(out);
        hessianOutput.setSerializerFactory(new SerializerFactory());

        hessianOutput.startReply();
        hessianOutput.writeObject(ret);
        hessianOutput.completeReply();
        hessianOutput.flush();
        out.close();

        // TODO Add error handling and method overloading

    }

}
