package org.fabric3.implementation.spring.runtime.component;

import static org.fabric3.host.Names.MONITOR_FACTORY_URI;
import static org.fabric3.host.Names.RUNTIME_MONITOR_CHANNEL_URI;
import static org.fabric3.host.runtime.BootConstants.APP_MONITOR;
import static org.fabric3.host.runtime.BootConstants.RUNTIME_MONITOR;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.host.Fabric3Exception;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.classloader.MaskingClassLoader;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootExports;
import org.fabric3.host.runtime.BootstrapFactory;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.host.runtime.BootstrapService;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HiddenPackages;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ScanResult;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.util.FileHelper;
import org.fabric3.implementation.spring.api.SpringMXBean;
import org.fabric3.spi.cm.ComponentManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

/**
 * @org.apache.xbean.XBean element="fabric3" rootElement="true"
 * @author ievdokimov
 * 
 */
public class ComponentManagerProxyFactoryBean implements FactoryBean<ComponentManager>, InitializingBean, DisposableBean, ApplicationContextAware, Ordered, SpringMXBean {

	private ComponentManager componentManager;
	private File runtimeLocation;
	private static final String DOMAIN = "fabric3";
	private static final String RUNTIME_MBEAN = "fabric3:SubDomain=runtime, type=component, name=RuntimeMBean";

	private RuntimeCoordinator coordinator;
	private ServerMonitor monitor;
	private ApplicationContext applicationContext;

	public void setRuntimeLocation(File loc) {
		this.runtimeLocation = loc;
	}

	public void afterPropertiesSet() throws Exception {

		if (applicationContext != null) {
			try {
				componentManager = BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, ComponentManager.class);
			} catch (NoSuchBeanDefinitionException e) {
			}
		}

		if (componentManager == null) {

			Assert.notNull(runtimeLocation, "Standalone runtime location must be specified");

			Assert.isTrue(runtimeLocation.exists(), "Runtime directory doesn't exists " + runtimeLocation.getAbsolutePath());

			start(runtimeLocation, new File(runtimeLocation, "config"), new File(runtimeLocation, "extentions"));
		}
	}

	public ComponentManager getObject() throws Exception {
		return componentManager;
	}

	public Class<?> getObjectType() {
		return ComponentManager.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Starts the runtime
	 * 
	 * @param runtimeDir
	 * @param configDir
	 * @param extensionsDir
	 * 
	 * @param params
	 *            the runtime parameters
	 * @throws Fabric3Exception
	 *             if catastrophic exception was encountered leaving the runtime
	 *             in an unstable state
	 */
	private void start(File runtimeDir, File configDir, File extensionsDir) throws Fabric3Exception {
		try {
			List<File> requiredDirectories = Arrays.asList(
					new File(runtimeDir, "repository"),
					new File(runtimeDir, "tmp"), 
					new File(runtimeDir, "data"));
			for (File file : requiredDirectories) {
				if (!file.exists()) {
					file.mkdir();
				}
			}

			File bootDir = BootstrapHelper.getDirectory(runtimeDir, "boot");
			File hostDir = BootstrapHelper.getDirectory(runtimeDir, "host");
			// add LogBack to exports
			BootExports.addExport("org.slf4j.*", "1.6.4");

			// create the classloaders for booting the runtime
			ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
			ClassLoader maskingClassLoader = new MaskingClassLoader(systemClassLoader, HiddenPackages.getPackages());
			ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, hostDir);
			ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

			BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

			// load the system configuration
			Document systemConfig = bootstrapService.createDefaultSystemConfig();

			URI domainName = bootstrapService.parseDomainName(systemConfig);

			RuntimeMode mode = bootstrapService.parseRuntimeMode(systemConfig);

			String environment = bootstrapService.parseEnvironment(systemConfig);

			String zoneName = bootstrapService.parseZoneName(systemConfig);

			String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, "vm", mode);

			List<File> deployDirs = bootstrapService.parseDeployDirectories(systemConfig);

			// create the HostInfo and runtime
			HostInfo hostInfo = BootstrapHelper.createHostInfo(runtimeName, mode, domainName, environment, runtimeDir, configDir, extensionsDir, deployDirs);

			// clear out the tmp directory
			FileHelper.cleanDirectory(hostInfo.getTempDir());

			BootConfiguration configuration = new BootConfiguration();

			MBeanServer mbServer = MBeanServerFactory.createMBeanServer(DOMAIN);

			// create and configure the monitor dispatchers
			MonitorEventDispatcher runtimeDispatcher = bootstrapService.createMonitorDispatcher(RUNTIME_MONITOR, systemConfig, hostInfo);
			MonitorEventDispatcher appDispatcher = bootstrapService.createMonitorDispatcher(APP_MONITOR, systemConfig, hostInfo);

			RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mbServer, runtimeDispatcher, appDispatcher, null);

			Fabric3Runtime runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

			ScanResult result = bootstrapService.scanRepository(hostInfo);

			URL systemComposite = new File(bootDir, "system.composite").toURI().toURL();

			configuration.setRuntime(runtime);
			configuration.setHostClassLoader(hostLoader);
			configuration.setBootClassLoader(bootLoader);
			configuration.setSystemCompositeUrl(systemComposite);
			configuration.setSystemConfig(systemConfig);
			configuration.setExtensionContributions(result.getExtensionContributions());
			configuration.setUserContributions(result.getUserContributions());

			// start the runtime
			coordinator = bootstrapService.createCoordinator(configuration);
			coordinator.start();

			// register the runtime with the MBean server
			ObjectName objectName = new ObjectName(RUNTIME_MBEAN);
			mbServer.registerMBean(this, objectName);

			MonitorProxyService monitorService = runtime.getComponent(MonitorProxyService.class, MONITOR_FACTORY_URI);
			monitor = monitorService.createMonitor(ServerMonitor.class, RUNTIME_MONITOR_CHANNEL_URI);
			monitor.started(mode.toString(), environment);
			componentManager = runtime.getComponent(ComponentManager.class);
		} catch (RuntimeException ex) {
			shutdown();
			handleStartException(ex);
		} catch (Exception ex) {
			shutdown();
			handleStartException(ex);
		}
	}

	private void shutdown() {
		try {
			if (coordinator != null) {
				coordinator.shutdown();
			}
		} catch (ShutdownException ex) {
			monitor.shutdownError(ex);
		}
	}

	private void handleStartException(Exception ex) throws Fabric3Exception {
		if (monitor != null) {
			// there could have been an error initializing the monitor
			monitor.exited(ex);
		} else {
			ex.printStackTrace();
		}
	}

	public interface ServerMonitor {

		@Severe("Shutdown error")
		void shutdownError(Exception e);

		@Info("Fabric3 ready [Mode:{0}, Environment: {1}]")
		void started(String mode, String environment);

		@Info("Fabric3 shutdown")
		void stopped();

		@Info("Fabric3 exited abnormally, Caused by")
		void exited(Exception e);

	}

	public void destroy() throws Exception {
		shutdown();
	}

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.applicationContext = ctx;
	}

	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
