/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.ws.axis2.runtime.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Flow;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.work.WorkScheduler;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class F3ConfiguratorImpl implements F3Configurator {
    private WorkScheduler scheduler;
    private HostInfo info;
    private ConfigurationContext configurationContext;
    private String servicePath = "axis2";
    private Map<String, AxisModule> modules = new HashMap<String, AxisModule>();
    private ClassLoader extensionClassLoader;
    private String chunkTransferEncoding = "true";
    private String cacheLargeAttachements = "true";
    private String cacheThreshold = "100000";

    public F3ConfiguratorImpl(@Reference WorkScheduler scheduler, @Reference HostInfo info) {
        this.scheduler = scheduler;
        this.info = info;
    }

    /**
     * @param servicePath Service path for Axis requests.
     */
    @Property
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    /**
     * TODO Make configurable: FABRICTHREE-276
     *
     * @param val true if large MTOM attachments should be streamed to disk to avoid buffering in memory. Note, Axis2 requires String values.
     */
    @Property
    public void setCacheLargeAttachements(String val) {
        this.cacheLargeAttachements = val;
    }

    /**
     * TODO Make configurable: FABRICTHREE-276
     *
     * @param threshold the file size threshold to cache to disk if MTOM file caching is enabled. Note, Axis2 requires String values.
     */
    @Property
    public void setCacheThreshold(String threshold) {
        this.cacheThreshold = threshold;
    }

    /**
     * TODO Make configurable: FABRICTHREE-276
     *
     * @param val true if chunked encoding should be used. Note, Axis2 requires String values.
     */
    @Property
    public void setChunkTransferEncoding(String val) {
        this.chunkTransferEncoding = val;
    }

    @Init
    public void start() throws Exception {

        configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
        configurationContext.setServicePath(servicePath);

        // configure Axis to use the F3 thread pool
        F3ThreadFactory factory = new F3ThreadFactory(scheduler);
        configurationContext.setThreadPool(factory);

        // set chunked transfer encoding 
        configurationContext.setProperty(HTTPConstants.CHUNKED, chunkTransferEncoding);

        // setup streaming large attachements to disk to avoid buffering in memory
        configurationContext.setProperty(Constants.Configuration.CACHE_ATTACHMENTS, cacheLargeAttachements);
        configurationContext.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, cacheThreshold);
        File dir = info.getTempDir();
        File attachementDir = new File(dir, "axis2");
        attachementDir.mkdir();
        configurationContext.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR, attachementDir.toString());


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
