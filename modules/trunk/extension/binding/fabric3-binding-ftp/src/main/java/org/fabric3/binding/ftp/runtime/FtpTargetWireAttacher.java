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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ftp.provision.FtpWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.expression.ExpressionExpander;
import org.fabric3.spi.services.expression.ExpressionExpansionException;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class FtpTargetWireAttacher implements TargetWireAttacher<FtpWireTargetDefinition> {
    private ExpressionExpander expander;

    public FtpTargetWireAttacher(@Reference ExpressionExpander expander) {
        this.expander = expander;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, FtpWireTargetDefinition target, Wire wire) throws WiringException {

        InvocationChain invocationChain = wire.getInvocationChains().values().iterator().next();
        URI uri = expandUri(target.getUri());
        try {
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 23 : uri.getPort();
            InetAddress hostAddress = "localhost".equals(host) ? InetAddress.getLocalHost() : InetAddress.getByName(host);

            invocationChain.addInterceptor(new FtpTargetInterceptor(hostAddress, port, target.getSecurity(), target.isActive()));
        } catch (UnknownHostException e) {
            throw new WiringException(e);
        }

    }

    public ObjectFactory<?> createObjectFactory(FtpWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    /**
     * Expands the target URI if it contains an expression of the form ${..}.
     *
     * @param uri the target uri to expand
     * @return the expanded URI with sourced values for any expressions
     * @throws WiringException if there is an error expanding an expression
     */
    private URI expandUri(URI uri) throws WiringException {
        try {
            String decoded = URLDecoder.decode(uri.toString(), "UTF-8");
            return URI.create(expander.expand(decoded));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        } catch (ExpressionExpansionException e) {
            throw new WiringException(e);
        }
    }

}
