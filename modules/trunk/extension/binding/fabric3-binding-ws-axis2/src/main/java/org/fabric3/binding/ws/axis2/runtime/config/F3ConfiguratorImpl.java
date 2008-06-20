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
package org.fabric3.binding.ws.axis2.runtime.config;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, AxisModule> modules = new HashMap<String, AxisModule>();
    private ClassLoader extensionClassLoader;

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

        Enumeration<URL> modules = classLoader.getResources("META-INF/module.xml");

        while (modules.hasMoreElements()) {

            AxisModule axisModule = new AxisModule();
            axisModule.setParent(axisConfiguration);
            axisModule.setModuleClassLoader(classLoader);

            InputStream moduleStream = modules.nextElement().openStream();
            ModuleBuilder moduleBuilder = new ModuleBuilder(moduleStream, axisModule, axisConfiguration);
            moduleBuilder.populateModule();

            addNewModule(axisModule, axisConfiguration);

        }

        org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfiguration.getModules(), axisConfiguration);
        axisConfiguration.validateSystemPredefinedPhases();

    }

    public void registerExtensionClassLoader(ClassLoader loader) {
        extensionClassLoader = loader;
    }

    public ClassLoader getExtensionClassLoader() {
        if (extensionClassLoader == null) {
            return getClass().getClassLoader();
        }
        return extensionClassLoader;
    }

    public AxisModule getModule(String name) {
        return modules.get(name);
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    private void addNewModule(AxisModule axisModule, AxisConfiguration axisConfiguration) throws AxisFault {

        ClassLoader moduleClassLoader = axisModule.getModuleClassLoader();

        addFlowHandlers(axisModule.getInFlow(), moduleClassLoader);
        addFlowHandlers(axisModule.getOutFlow(), moduleClassLoader);
        addFlowHandlers(axisModule.getFaultInFlow(), moduleClassLoader);
        addFlowHandlers(axisModule.getFaultOutFlow(), moduleClassLoader);

        axisConfiguration.addModule(axisModule);

        modules.put(axisModule.getName(), axisModule);

    }

    private void addFlowHandlers(Flow flow, ClassLoader moduleClassLoader) throws AxisFault {
        if (flow != null) {
            Utils.addFlowHandlers(flow, moduleClassLoader);
        }
    }


}
