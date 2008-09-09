package org.fabric3.weblogic92.console;

import javax.management.ObjectName;

import org.fabric3.weblogic92.console.service.DefaultDomainTopologyService;
import org.fabric3.weblogic92.console.service.DefaultJmxConnectionService;
import org.fabric3.weblogic92.console.service.F3Runtime;
import org.fabric3.weblogic92.console.service.JmxConnectionService;
import org.fabric3.weblogic92.console.service.Server;

public class Test {

	public static void main(String[] args) throws Exception {
		
		ObjectName domainRuntimeServiceMBeanName = 
			new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		
		JmxConnectionService jmxConnectionService = new DefaultJmxConnectionService();
		
		DefaultDomainTopologyService domainService = new DefaultDomainTopologyService();
		domainService.setDomainServer("weblogic.management.mbeanservers.domainruntime");
		domainService.setRuntimeServer("weblogic.management.mbeanservers.runtime");
		domainService.setDomainRuntimeServiceMBeanName(domainRuntimeServiceMBeanName);
		domainService.setJmxConnectionService(jmxConnectionService);
		
		for (Server server : domainService.getDomainTopology("localhost", 7001, "weblogic", "password")) {
			for (F3Runtime f3Runtime : server.getF3Runtimes()) {
				System.err.println("F3 Runtime:" + f3Runtime.getSubDomain());
			}
		}
		
	}

}
