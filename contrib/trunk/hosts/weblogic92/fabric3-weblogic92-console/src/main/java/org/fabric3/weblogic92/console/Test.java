package org.fabric3.weblogic92.console;

import javax.management.ObjectName;

import org.fabric3.weblogic92.console.service.DefaultDomainService;
import org.fabric3.weblogic92.console.service.DefaultJmxConnectionService;
import org.fabric3.weblogic92.console.service.JmxConnectionService;
import org.fabric3.weblogic92.console.service.Server;

public class Test {
		;

	/*
	 * Initialize connection to the Domain Runtime MBean Server.
	 */
	public static void initConnection() throws Exception {
		
	}

	public static void main(String[] args) throws Exception {
		
		ObjectName domainRuntimeServiceMBeanName = 
			new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		
		JmxConnectionService jmxConnectionService = new DefaultJmxConnectionService();
		
		DefaultDomainService domainService = new DefaultDomainService();
		domainService.setMbeanServer("/jndi/weblogic.management.mbeanservers.domainruntime");
		domainService.setDomainRuntimeServiceMBeanName(domainRuntimeServiceMBeanName);
		domainService.setJmxConnectionService(jmxConnectionService);
		
		for (Server server : domainService.getServers("localhost", 7001, "weblogic", "password")) {
			System.err.println(server.getName() + ":" + server.getListenAddress() + ":" + server.getListenPort() + ":" + server.getState());
		}
		
	}

}
