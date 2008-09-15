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
package org.fabric3.binding.tcp.runtime.wire;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.tcp.provision.TCPWireSourceDefinition;
import org.fabric3.binding.tcp.runtime.concurrent.F3ExecutorService;
import org.fabric3.binding.tcp.runtime.handler.TCPHandler;
import org.fabric3.binding.tcp.runtime.monitor.TCPBindingMonitor;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class TCPSourceWireAttacher implements SourceWireAttacher<TCPWireSourceDefinition> {

    private final WorkScheduler workScheduler;

    private final TCPBindingMonitor monitor;

    private SocketAcceptor acceptor;

    /**
     * Inject dependencies
     * 
     * @param workScheduler F3 Work Scheduler
     */
    public TCPSourceWireAttacher(@Reference WorkScheduler workScheduler, @Monitor TCPBindingMonitor monitor) {
	this.workScheduler = workScheduler;
	this.monitor = monitor;
    }

    /**
     * {@inheritDoc}
     */
    public void attachToSource(TCPWireSourceDefinition source, PhysicalWireTargetDefinition target, final Wire wire)
	    throws WiringException {
	URI uri = source.getUri();
	String hostname = uri.getHost();
	int port = uri.getPort();

	ExecutorService filterExecutor = new F3ExecutorService(workScheduler);
	InetSocketAddress socketAddress;
	try {
	    // TODO: Move below to separate component configurable with Codec
	    // and IoHandler based on protocol like TCP/HTTP etc.
	    socketAddress = new InetSocketAddress(hostname, port);
	    acceptor = new NioSocketAcceptor();
	    acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(filterExecutor));
	    acceptor.getFilterChain().addLast("codec",
		    new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

	    acceptor.setHandler(new TCPHandler(wire, monitor));
	    acceptor.bind(socketAddress);

	    monitor.onTcpExtensionStarted(uri.toString());

	} catch (UnknownHostException exception) {
	    throw new WiringException("Unable to bind to:" + uri.toString(), "binding.tcp", exception);
	} catch (IOException exception) {
	    throw new WiringException("Unable to bind to:" + uri.toString(), "binding.tcp", exception);
	}

    }

    /**
     * {@inheritDoc}
     */
    public void detachFromSource(TCPWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
	    throws WiringException {
	throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void attachObjectFactory(TCPWireSourceDefinition source, ObjectFactory<?> objectFactory,
	    PhysicalWireTargetDefinition definition) throws WiringException {
	throw new UnsupportedOperationException();
    }

    /**
     * Stops the processing.
     */
    @Destroy
    public void destroy() {
	acceptor.unbind();
	acceptor.dispose();
    }
}
