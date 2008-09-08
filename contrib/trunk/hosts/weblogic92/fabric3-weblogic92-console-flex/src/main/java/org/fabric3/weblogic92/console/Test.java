package org.fabric3.weblogic92.console;

import javax.management.ObjectName;

import org.fabric3.weblogic92.console.service.DefaultDomainService;
import org.fabric3.weblogic92.console.service.DefaultF3RuntimeService;
import org.fabric3.weblogic92.console.service.DefaultJmxConnectionService;
import org.fabric3.weblogic92.console.service.F3Runtime;
import org.fabric3.weblogic92.console.service.JmxConnectionService;
import org.fabric3.weblogic92.console.service.Server;

public class Test {

	public static void main(String[] args) throws Exception {
		
		ObjectName domainRuntimeServiceMBeanName = 
			new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		
		JmxConnectionService jmxConnectionService = new DefaultJmxConnectionService();
		
		DefaultDomainService domainService = new DefaultDomainService();
		domainService.setMbeanServer("weblogic.management.mbeanservers.domainruntime");
		domainService.setDomainRuntimeServiceMBeanName(domainRuntimeServiceMBeanName);
		domainService.setJmxConnectionService(jmxConnectionService);
		
		DefaultF3RuntimeService f3RuntimeService = new DefaultF3RuntimeService();
		f3RuntimeService.setMbeanServer("weblogic.management.mbeanservers.runtime");
		f3RuntimeService.setJmxConnectionService(jmxConnectionService);
		
		for (Server server : domainService.getServers("localhost", 7001, "weblogic", "password")) {
			System.err.println("Server: " + server.getName());
			for (F3Runtime f3Runtime : f3RuntimeService.getF3Runtimes(server, "weblogic", "password")) {
				System.err.println("F3 Runtime:" + f3Runtime.getSubDomain());
			}
		}
		
	}

}
