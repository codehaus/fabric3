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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.fabric3.spi.wire.Wire;

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;
import com.caucho.burlap.io.SerializerFactory;

/**
 * Servlet for handling the hessian service requests.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class BurlapServiceHandler extends HttpServlet {    

    /**
     * Wire attached to the servlet.
     */
    private Wire wire;
    
    /**
     * Map of op names to operation definitions.
     */
    private Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;
    
    /**
     * Initializes the wire associated with the service.
     * @param wire Wire that connects the transport to the component.
     * @param ops Map of op names to operation definitions.
     */
    public BurlapServiceHandler(Wire wire, Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops) {
        this.wire = wire;
        this.ops = ops;
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InputStream in = request.getInputStream();
        
        BurlapInput burlapInput = new BurlapInput(in);
        burlapInput.setSerializerFactory(new SerializerFactory());
        burlapInput.startCall();
        
        String methodName = burlapInput.getMethod();
        
        PhysicalOperationDefinition op = ops.get(methodName).getKey();
        Interceptor head = ops.get(methodName).getValue().getHeadInterceptor();
            
        Object[] args = new Object[op.getParameters().size()];
        for(int i = 0;i < args.length;i++) {
            args[i] = burlapInput.readObject();
        }
        burlapInput.completeCall();
        
        Message input = new MessageImpl(args, false, new SimpleWorkContext(), wire);
                
        Message output = head.invoke(input); 
        Object ret = output.getBody();
                
        OutputStream out = response.getOutputStream();
        BurlapOutput burlapOutput = new BurlapOutput(out);
                
        burlapOutput.startReply();
        burlapOutput.writeObject(ret);
        burlapOutput.completeReply();
        
        out.close();
        
        // TODO Add error handling and method overloading
        
    }

}
