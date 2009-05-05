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
package org.fabric3.binding.net.runtime.http;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
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
import org.fabric3.binding.net.config.HttpConfig;
import org.fabric3.binding.net.provision.HttpWireTargetDefinition;
import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.runtime.OneWayClientHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.util.OperationTypeHelper;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.SerializerFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches references to an HTTP channel.
 *
 * @version $Revision$ $Date$
 */
public class HttpTargetWireAttacher implements TargetWireAttacher<HttpWireTargetDefinition> {
    private long connectTimeout = 10000;
    private int retries = 0;
    private String httpWireFormat = "jdk";
    private ClassLoaderRegistry classLoaderRegistry;
    private NetBindingMonitor monitor;
    private ChannelFactory factory;
    private Timer timer;
    private Map<String, SerializerFactory> serializerFactories;

    public HttpTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry, @Monitor NetBindingMonitor monitor) {
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
    public void setHttpWireFormat(String httpWireFormat) {
        this.httpWireFormat = httpWireFormat;
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

    public void attachToTarget(PhysicalWireSourceDefinition source, HttpWireTargetDefinition target, Wire wire) throws WiringException {

        for (InvocationChain chain : wire.getInvocationChains()) {
            if (chain.getPhysicalOperation().isOneWay()) {
                attachOneWay(target, chain);
            } else {
                attachRequestResponse(target, chain);
            }
        }
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, HttpWireTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(HttpWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private void attachOneWay(HttpWireTargetDefinition target, InvocationChain chain) throws WiringException {

        HttpConfig config = target.getConfig();
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
        HttpClientPipelineFactory pipeline = new HttpClientPipelineFactory(handler, timer, timeout);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
        // TODO support method overloading
        PhysicalOperationDefinition operation = chain.getPhysicalOperation();
        String name = operation.getName();


        Serializer headerSerializer = getHeaderSerializer();
        Serializer inputSerializer = getInputSerializer(target, operation);
        HttpOneWayInterceptor interceptor =
                new HttpOneWayInterceptor(path, name, address, headerSerializer, inputSerializer, bootstrap, retryCount, monitor);
        chain.addInterceptor(interceptor);
    }


    private void attachRequestResponse(HttpWireTargetDefinition target, InvocationChain chain) throws WiringException {

        HttpConfig config = target.getConfig();
        int retryCount = this.retries;
        if (config.getNumberOfRetries() > -1) {
            retryCount = config.getNumberOfRetries();
        }

        long timeout = connectTimeout;
        if (config.getReadTimeout() > -1) {
            timeout = config.getReadTimeout();
        }

        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        HttpResponseHandler handler = new HttpResponseHandler(connectTimeout, monitor);
        HttpClientPipelineFactory pipeline = new HttpClientPipelineFactory(handler, timer, timeout);
        bootstrap.setPipelineFactory(pipeline);

        URI uri = target.getUri();
        String path = uri.getPath();

        InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
        // TODO support method overloading
        PhysicalOperationDefinition operation = chain.getPhysicalOperation();
        String name = operation.getName();
        Serializer headerSerializer = getHeaderSerializer();
        Serializer inputSerializer = getInputSerializer(target, operation);
        Serializer outputSerializer = getOutputSerializer(target, operation);
        HttpRequestResponseInterceptor interceptor =
                new HttpRequestResponseInterceptor(path, name, headerSerializer, inputSerializer, outputSerializer, address, bootstrap, retryCount);
        chain.addInterceptor(interceptor);
    }

    private Serializer getHeaderSerializer() throws WiringException {
        SerializerFactory headerSerializerFactory = serializerFactories.get(httpWireFormat);
        if (headerSerializerFactory == null) {
            throw new WiringException("Header serializer not found for: " + httpWireFormat);
        }
        try {
            // TODO FIXME
            return headerSerializerFactory.getInstance(Collections.<Class<?>>emptySet(), Collections.<Class<?>>emptySet());
        } catch (SerializationException e) {
            throw new WiringException(e);
        }
    }

    private Serializer getInputSerializer(HttpWireTargetDefinition target, PhysicalOperationDefinition operation) throws WiringException {
        try {
            String wireFormat = target.getConfig().getWireFormat();
            if (wireFormat == null) {
                wireFormat = "jaxb";
            }
            SerializerFactory messageSerializerFactory = serializerFactories.get(wireFormat);
            if (messageSerializerFactory == null) {
                throw new WiringException("Serializer not found for: " + wireFormat);
            }
            ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            Set<Class<?>> inputTypes = OperationTypeHelper.loadInParameterTypes(operation, loader);
            return messageSerializerFactory.getInstance(inputTypes, Collections.<Class<?>>emptySet());
        } catch (SerializationException e) {
            throw new WiringException(e);
        }

    }

    private Serializer getOutputSerializer(HttpWireTargetDefinition target, PhysicalOperationDefinition operation) throws WiringException {
        try {
            String wireFormat = target.getConfig().getWireFormat();
            if (wireFormat == null) {
                wireFormat = "jaxb";
            }
            SerializerFactory messageSerializerFactory = serializerFactories.get(wireFormat);
            if (messageSerializerFactory == null) {
                throw new WiringException("Serializer not found for: " + wireFormat);
            }
            ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            Set<Class<?>> faultTypes = OperationTypeHelper.loadFaultTypes(operation, loader);
            Set<Class<?>> outputTypes = OperationTypeHelper.loadOutputTypes(operation, loader);
            return messageSerializerFactory.getInstance(outputTypes, faultTypes);
        } catch (SerializationException e) {
            throw new WiringException(e);
        }

    }

}
