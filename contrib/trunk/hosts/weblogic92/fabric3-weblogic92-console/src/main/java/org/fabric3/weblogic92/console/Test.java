package org.fabric3.weblogic92.console;

import org.fabric3.weblogic92.console.service.DefaultDomainTopologyService;
import org.fabric3.weblogic92.console.service.DefaultJmxConnectionService;
import org.fabric3.weblogic92.console.service.JmxConnectionService;

public class Test {

	public static void main(String[] args) throws Exception {
		
		JmxConnectionService jmxConnectionService = new DefaultJmxConnectionService();
		
		DefaultDomainTopologyService domainService = new DefaultDomainTopologyService();
		domainService.setDomainServer("weblogic.management.mbeanservers.domainruntime");
		domainService.setRuntimeServer("weblogic.management.mbeanservers.runtime");
		domainService.setDomainRuntimeService(
				"com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		domainService.setJmxConnectionService(jmxConnectionService);
		
		System.err.println(domainService.getDomainTopologyAsXml("localhost", 7001, "weblogic", "password"));
		
	}

}
