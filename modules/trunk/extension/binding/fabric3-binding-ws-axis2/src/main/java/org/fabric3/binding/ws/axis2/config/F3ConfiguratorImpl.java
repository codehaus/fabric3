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
package org.fabric3.binding.ws.axis2.config;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Flow;
import org.apache.axis2.engine.AxisConfiguration;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class F3ConfiguratorImpl implements F3Configurator {
    
    private ConfigurationContext configurationContext;
    private String servicePath = "axis2";
    
    /**
     * @param servicePath Service path for Axis requests.
     */
    @Property
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }
    
    
    @Init
    public void start() throws Exception {
        
        configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
        configurationContext.setServicePath(servicePath);
        
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        
        ClassLoader classLoader = getClass().getClassLoader();
        
        int count = 0;
        Enumeration<URL> modules = classLoader.getResources("META-INF/module.xml");
        
        while (modules.hasMoreElements()) {
            
            AxisModule axisModule = new AxisModule(String.valueOf(++count));
            axisModule.setParent(axisConfiguration);
            axisModule.setModuleClassLoader(classLoader);

            InputStream moduleStream = modules.nextElement().openStream();
            ModuleBuilder moduleBuilder = new ModuleBuilder(moduleStream, axisModule, axisConfiguration);
            moduleBuilder.populateModule();
            
            addNewModule(axisModule, axisConfiguration);
            
            System.err.println("Added new module");
            
        }
        
        org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfiguration.getModules(), axisConfiguration);
        axisConfiguration.validateSystemPredefinedPhases();
        
    }
    
    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    private void addNewModule(AxisModule modulemetadata, AxisConfiguration axisConfiguration) throws AxisFault {
        
        ClassLoader moduleClassLoader = modulemetadata.getModuleClassLoader();

        addFlowHandlers(modulemetadata.getInFlow(), moduleClassLoader);
        addFlowHandlers(modulemetadata.getOutFlow(), moduleClassLoader);
        addFlowHandlers(modulemetadata.getFaultInFlow(), moduleClassLoader);
        addFlowHandlers(modulemetadata.getFaultOutFlow(), moduleClassLoader);

        axisConfiguration.addModule(modulemetadata);
        
    }
    
    private void addFlowHandlers(Flow flow, ClassLoader moduleClassLoader) throws AxisFault {
        if (flow != null) {
            Utils.addFlowHandlers(flow, moduleClassLoader);
        }
    }
    

}
