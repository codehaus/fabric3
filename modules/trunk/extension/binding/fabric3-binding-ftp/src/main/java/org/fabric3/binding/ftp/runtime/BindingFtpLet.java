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

import org.fabric3.ftp.api.FtpLet;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Wire;

/**
 * Handles incoming FTP puts from the protocol stack.
 *
 * @version $Revision$ $Date$
 */
public class BindingFtpLet implements FtpLet {
    private String servicePath;
    private Wire wire;
    private Interceptor interceptor;
    private BindingMonitor monitor;

    public BindingFtpLet(String servicePath, Wire wire, BindingMonitor monitor) {
        this.servicePath = servicePath;
        this.wire = wire;
        this.monitor = monitor;
    }

    public boolean onUpload(String fileName, InputStream uploadData) throws Exception {
        Object[] args = new Object[]{fileName, uploadData};
        WorkContext workContext = new WorkContext();
        Message input = new MessageImpl(args, false, workContext);
        Message msg = getInterceptor().invoke(input);
        if (msg.isFault()) {
            monitor.fileProcessingError(servicePath, (Throwable) msg.getBody());
            return false;
        }
        return true;
    }

    private Interceptor getInterceptor() {
        // lazy load the interceptor as it may not have been added when the instance was created in the wire attacher
        if (interceptor == null) {
            interceptor = wire.getInvocationChains().values().iterator().next().getHeadInterceptor();
        }
        return interceptor;
    }
}
