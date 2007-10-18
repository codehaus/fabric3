/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.binding.ws.cxf;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.servlet.F3Dispatcher;
import org.apache.cxf.transport.servlet.ServletTransportFactory;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.wire.WsTargetInterceptor;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Default CXFService implementation. Boostraps, configures, and manages CXF.
 *
 * @version $Rev$ $Date$
 */
public class CXFServiceImpl implements CXFService {
    private Bus bus;
    private F3Dispatcher dispatcher;
    private ServletHost host;
    private String contextPath;
    private CXFMonitor monitor;
    private Level cxfLogLevel = Level.WARNING;

    /**
     * Constructor.
     *
     * @param host           the servlet host to register endpoints with
     * @param monitorFactory the monitor factory
     * @param contextPath    context path from which the web services are provisioned.
     */
    public CXFServiceImpl(@Reference(name = "host")ServletHost host,
                          @Reference(name = "monitorFactory")MonitorFactory monitorFactory,
                          @Property(name = "contextPath")String contextPath) {
        this.host = host;
        this.contextPath = contextPath;
        monitor = monitorFactory.getMonitor(CXFMonitor.class);
    }

    @Property
    public void setCxfLogLevel(String level) {
        this.cxfLogLevel = Level.parse(level);
    }

    @Init
    public void init() throws BusException {
        bus = initializeBus();
        dispatcher = initializeDispatcher();
        // register the dispatcher to handle all requests for the context
        host.registerMapping(contextPath, dispatcher);
        monitor.extensionStarted();
        Logger.getLogger("org.apache.cxf").setLevel(cxfLogLevel);
    }

    @Destroy
    public void destroy() {
        bus.shutdown(true);
        monitor.extensionStopped();
    }

    public void provisionEndpoint(URI uri, Class<?> interfaze, Wire wire) {
        Map<String, InvocationChain> interceptors = new HashMap<String, InvocationChain>();
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            interceptors.put(entry.getKey().getName(), entry.getValue());
        }
        // TODO at some point we should use dynamic invocation mechanism instead of generating a proxy
        Object implementor = ServiceProxyHandler.newInstance(interfaze, interceptors, wire);
        ServerFactoryBean serverFactoryBean = new ServerFactoryBean();
        serverFactoryBean.setBus(bus);
        String address = uri.toASCIIString();
        serverFactoryBean.setAddress(address);
        serverFactoryBean.setServiceClass(interfaze);
        serverFactoryBean.setServiceBean(implementor);
        // add a mapping so the service will be dispatched to by the ServletHost
        host.registerMapping(address, dispatcher);
        serverFactoryBean.create();
        endpointProvisioned(address);
    }

    public void bindToTarget(URI uri, Class<?> interfaze, Wire wire) {
        ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
        factory.setBus(bus);
        factory.setServiceClass(interfaze);
        factory.setAddress(uri.toString());
        Object proxy = interfaze.cast(factory.create());
        for (Method method : interfaze.getDeclaredMethods()) {
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                InvocationChain chain = entry.getValue();
                if (method.getName().equals(op.getName())) {
                    chain.addInterceptor(new WsTargetInterceptor(method, proxy));
                }
            }
        }
    }

    /**
     * Bootstraps the underlying bus implementation
     *
     * @return the underlying bus implementation
     */
    private Bus initializeBus() {
        CXFBusFactory factory = new CXFBusFactory();
        Bus bus = factory.createBus();
        ServletTransportFactory transportFactory = new ServletTransportFactory();
        // TODO make configurable
        List<String> ids = new ArrayList<String>();
        ids.add("http://cxf.apache.org/bindings/xformat");
        ids.add("http://schemas.xmlsoap.org/soap/http");
        ids.add("http://schemas.xmlsoap.org/wsdl/http/");
        ids.add("http://schemas.xmlsoap.org/wsdl/soap/http");
        ids.add("http://www.w3.org/2003/05/soap/bindings/HTTP/");
        ids.add("http://cxf.apache.org/transports/http/configuration");
        transportFactory.setTransportIds(ids);
        transportFactory.setBus(bus);
        DestinationFactoryManager dfMgr = bus.getExtension(DestinationFactoryManager.class);
        dfMgr.registerDestinationFactory("http://cxf.apache.org/transports/http/configuration", transportFactory);
        dfMgr.registerDestinationFactory("http://www.w3.org/2003/05/soap/bindings/HTTP/", transportFactory);
        dfMgr.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", transportFactory);
        dfMgr.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/http/", transportFactory);
        dfMgr.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", transportFactory);
        dfMgr.registerDestinationFactory("http://cxf.apache.org/bindings/xformat", transportFactory);
        SoapBindingFactory bindingFactory = new SoapBindingFactory();
        bindingFactory.setBus(bus);
        BindingFactoryManager bfMgr = bus.getExtension(BindingFactoryManager.class);
        bfMgr.registerBindingFactory("http://schemas.xmlsoap.org/soap/", bindingFactory);
        bfMgr.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap", bindingFactory);
        bfMgr.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap12", bindingFactory);
        bfMgr.registerBindingFactory("http://www.w3.org/2003/05/soap/bindings/HTTP", bindingFactory);
        bfMgr.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/http", bindingFactory);
        bfMgr.registerBindingFactory("http://schemas.xmlsoap.org/soap/http", bindingFactory);
        ConduitInitiatorManager ciMgr = bus.getExtension(ConduitInitiatorManager.class);
        SoapTransportFactory soapTransportFactory = new SoapTransportFactory();
        soapTransportFactory.setBus(bus);
        ciMgr.registerConduitInitiator("http://schemas.xmlsoap.org/soap/", soapTransportFactory);
        ciMgr.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/", soapTransportFactory);
        ciMgr.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap12/", soapTransportFactory);
        F3ConduitInitiator initiator = new F3ConduitInitiator();
        initiator.setBus(bus);
        ciMgr.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", initiator);
        return bus;
    }

    /**
     * Initializes the request dispatcher
     *
     * @return the request dispatcher
     * @throws BusException if an error initializing the dispatcher occurs
     */
    private F3Dispatcher initializeDispatcher() throws BusException {
        ServletTransportFactory transport;
        DestinationFactoryManager mgr = bus.getExtension(DestinationFactoryManager.class);
        DestinationFactory df = mgr.getDestinationFactory("http://cxf.apache.org/transports/http/configuration");
        if (df instanceof ServletTransportFactory) {
            transport = (ServletTransportFactory) df;
        } else {
            transport = new ServletTransportFactory(bus);
        }
        return new F3Dispatcher(bus, transport);
    }

    /**
     * Monitor callback after a service has been provisioned
     *
     * @param address the address of the provisioned service
     */
    private void endpointProvisioned(String address) {
        if (contextPath.length() > 0) {
            if (contextPath.endsWith("/*")) {
                address = contextPath.substring(0, contextPath.length() - 2) + address;
            } else {
                address = contextPath + address;
            }
        }
        monitor.endpointProvisioned(address);
    }

}