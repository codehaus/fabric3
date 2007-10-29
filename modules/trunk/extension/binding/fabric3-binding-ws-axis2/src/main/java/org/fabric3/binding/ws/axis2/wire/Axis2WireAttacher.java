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
package org.fabric3.binding.ws.axis2.wire;

import java.net.URI;

import javax.servlet.ServletContext;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.AxisServlet;
import org.fabric3.binding.ws.axis2.physical.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.physical.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.servlet.F3Axis2ServletConfig;
import org.fabric3.binding.ws.axis2.servlet.F3AxisServlet;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL contract
 */
@EagerInit
public class Axis2WireAttacher implements WireAttacher<Axis2WireSourceDefinition, Axis2WireTargetDefinition> {
    
    private ServletHost servletHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private WireAttacherRegistry wireAttacherRegistry;
    
    /**
     * Injects servlet host and classloader registry.
     * 
     * @param servletHost Servlet host.
     * @param classLoaderRegistry Classloader registry.
     */
    public Axis2WireAttacher(@Reference ServletHost servletHost, 
                             @Reference ClassLoaderRegistry classLoaderRegistry,
                             @Reference WireAttacherRegistry wireAttacherRegistry) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
        this.wireAttacherRegistry = wireAttacherRegistry;
    }
    
    /**
     * Registers with the wire attacher registry.
     */
    @Init
    public void start() {
        wireAttacherRegistry.register(Axis2WireSourceDefinition.class, this);
        wireAttacherRegistry.register(Axis2WireTargetDefinition.class, this);
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToSource(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, 
     *                                                                    org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, 
     *                                                                    org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        
        try {
            
            String uri = source.getUri().getPath();
            URI classLoaderUri = source.getClassloaderURI();
            String serviceClass = source.getServiceInterface();
            
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderUri);
            
            AxisService axisService = new AxisService();
            
            axisService.setName(uri);
            axisService.setDocumentation("Fabric3 enabled axis service");
            axisService.setClientSide(false);
            axisService.setClassLoader(classLoader);
            axisService.setEndpointURL(uri);
            
            Parameter interfaceParameter = new Parameter(Constants.SERVICE_CLASS, serviceClass);
            axisService.addParameter(interfaceParameter);
            
            ConfigurationContext configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
            
            Utils.fillAxisService(axisService, configurationContext.getAxisConfiguration(), null, null);
            
            configurationContext.getAxisConfiguration().addService(axisService);
            
            AxisServlet axisServlet = new F3AxisServlet(configurationContext);
            
            configurationContext.setContextRoot("/");
            
            servletHost.registerMapping(uri, axisServlet);
            
        } catch (Exception e) {
            throw new WiringException(e);
        }
        
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, 
     *                                                                    org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, 
     *                                                                    org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition source, Axis2WireTargetDefinition target, Wire wire)
            throws WiringException {
        // TODO Auto-generated method stub
    }

}
