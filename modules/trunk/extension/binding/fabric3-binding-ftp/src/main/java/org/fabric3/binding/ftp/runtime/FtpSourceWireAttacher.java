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
package org.fabric3.binding.ftp.runtime;

import java.io.InputStream;
import java.net.URI;

import org.fabric3.binding.ftp.provision.FtpWireSourceDefinition;
import org.fabric3.ftp.api.FtpLet;
import org.fabric3.ftp.spi.FtpLetContainer;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class FtpSourceWireAttacher implements SourceWireAttacher<FtpWireSourceDefinition> {
    
    private final FtpLetContainer ftpLetContainer;

    /**
     * Injects the references.
     * 
     * @param ftpLetContainer FtpLet container.
     */
    public FtpSourceWireAttacher(@Reference FtpLetContainer ftpLetContainer) {
        this.ftpLetContainer = ftpLetContainer;
    }

    public void attachToSource(FtpWireSourceDefinition source, PhysicalWireTargetDefinition target, final Wire wire) throws WiringException {
        
        URI uri = source.getUri();
        String servicePath = uri.getPath();
        ftpLetContainer.registerFtpLet(servicePath, new FtpLet() {
            public void onUpload(String fileName, InputStream uploadData) throws Exception {
                Interceptor head = wire.getInvocationChains().values().iterator().next().getHeadInterceptor();
                Object[] args = new Object[] {fileName, uploadData};
                WorkContext workContext = new WorkContext();
                Message input = new MessageImpl(args, false, workContext);
                head.invoke(input);
            }            
        });

    }

    public void detachFromSource(FtpWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();
    }

    public void attachObjectFactory(FtpWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }

}
