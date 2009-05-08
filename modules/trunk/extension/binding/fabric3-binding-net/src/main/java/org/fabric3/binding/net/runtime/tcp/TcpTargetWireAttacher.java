/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.net.runtime.tcp;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.HashedWheelTimer;
import org.jboss.netty.handler.timeout.Timer;
import org.oasisopen.sca.annotation.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.config.TcpConfig;
import org.fabric3.binding.net.provision.TcpWireTargetDefinition;
import org.fabric3.binding.net.runtime.OneWayClientHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.SerializerFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.util.OperationTypeHelper;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches references configured to use the TCP binding to the channel and handler pipeline.
 *
 * @version $Revision$ $Date$
 */
public class TcpTargetWireAttacher implements TargetWireAttacher<TcpWireTargetDefinition> {
    private long connectTimeout = 10000;
    private int retries = 0;
    private NetBindingMonitor monitor;
    private ChannelFactory factory;
    private Timer timer;
    private String tcpWireFormat = "jdk";
    private Map<String, SerializerFactory> serializerFactories = new HashMap<String, SerializerFactory>();
    private ClassLoaderRegistry classLoaderRegistry;

    public TcpTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry, @Monitor NetBindingMonitor monitor) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitor;
    }

    @Reference
    public void setSerializerFactories(Map<String, SerializerFactory> serializerFactories) {
        this.serializerFactories = serializerFactories;
    }

    @Property(required = false)
    public void setConnectTimeout(long timeout) {
        this.connectTimeout = timeout;
    }

    @Property(required = false)
    public void setTcpWireFormat(String tcpWireFormat) {
        this.tcpWireFormat = tcpWireFormat;
    }

    @Property(required = false)
    public void setRetries(int retries) {
        this.retries = retries;
    }

    @Init
    public void init() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        factory = new NioClientSocketChannelFactory(executorService, executorService);
        timer = new HashedWheelTimer();
    }

    @Destroy
    public void destroy() {
        if (factory != null) {
            factory.releaseExternalResources();
        }
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, TcpWireTargetDefinition target, Wire wire) throws WiringException {

        for (InvocationChain chain : wire.getInvocationChains()) {
            if (chain.getPhysicalOperation().isOneWay()) {
                attachOneWay(target, chain);
            } else {
                attachRequestResponse(target, chain);
            }
        }
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, TcpWireTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(TcpWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private void attachOneWay(TcpWireTargetDefinition target, InvocationChain chain) throws WiringException {
        TcpConfig config = target.getConfig();
        int retryCount = this.retries;
        if (config.getNumberOfRetries() > -1) {
            retryCount = config.getNumberOfRetries();
        }

        long timeout = connectTimeout;
        if (config.getReadTimeout() > -1) {
            timeout = config.getReadTimeout();
        }

        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        OneWayClientHandler handler = new OneWayClientHandler(monitor);
        TcpPipelineFactory pipeline = new TcpPipelineFactory(handler, timer, timeout);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
        // TODO support method overloading
        PhysicalOperationDefinition operation = chain.getPhysicalOperation();
        String name = operation.getName();

        Serializer serializer = getMessageSerializer(target, operation);
        TcpOneWayInterceptor interceptor = new TcpOneWayInterceptor(path, name, address, serializer, bootstrap, retryCount, monitor);
        chain.addInterceptor(interceptor);
    }

    private void attachRequestResponse(TcpWireTargetDefinition target, InvocationChain chain) throws WiringException {
        TcpConfig config = target.getConfig();
        int retryCount = this.retries;
        if (config.getNumberOfRetries() > -1) {
            retryCount = config.getNumberOfRetries();
        }

        long timeout = connectTimeout;
        if (config.getReadTimeout() > -1) {
            timeout = config.getReadTimeout();
        }

        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        PhysicalOperationDefinition operation = chain.getPhysicalOperation();
        Serializer serializer = getMessageSerializer(target, operation);
        TcpResponseHandler handler = new TcpResponseHandler(serializer, connectTimeout, monitor);
        TcpPipelineFactory pipeline = new TcpPipelineFactory(handler, timer, timeout);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
        // TODO support method overloading
        String name = operation.getName();
        TcpRequestResponseInterceptor interceptor = new TcpRequestResponseInterceptor(path, name, serializer, address, bootstrap, retryCount);
        chain.addInterceptor(interceptor);
    }

    private Serializer getMessageSerializer(TcpWireTargetDefinition target, PhysicalOperationDefinition operation) throws WiringException {
        try {
            String wireFormat = target.getConfig().getWireFormat();
            if (wireFormat == null) {
                wireFormat = tcpWireFormat;
            }
            SerializerFactory messageSerializerFactory = serializerFactories.get(wireFormat);
            if (messageSerializerFactory == null) {
                throw new WiringException("Serializer not found for: " + wireFormat);
            }
            ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            Set<Class<?>> types = OperationTypeHelper.loadInParameterTypes(operation, loader);
            return messageSerializerFactory.getInstance(types, Collections.<Class<?>>emptySet(), loader);
        } catch (SerializationException e) {
            throw new WiringException(e);
        }

    }

}