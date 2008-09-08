package org.fabric3.weblogic92.console.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Default implementation of the F3 runtime service.
 * 
 * @author meerajk
 *
 */
public class DefaultF3RuntimeService implements F3RuntimeService {
	
	private JmxConnectionService jmxConnectionService;
	private String mbeanServer;
	
	/**
	 * Gets all the F3 runtimes hosted on a server.
	 * 
	 * @param server Server that is queried.
	 * @param user User name used to connect.
	 * @param password Password used to connect.
	 * @return List of F3 runtimes.
	 * @throws IOException If unable to connect to the JMX server.
	 * @throws JMException In case of unexpected JMX exceptions.
	 */
	public Set<F3Runtime> getF3Runtimes(Server server, String user, String password) throws IOException, JMException {
		
		JMXConnector connector = jmxConnectionService.getConnector(server.getAddress(), server.getPort(), mbeanServer, user, password);
	
		try {
			
			MBeanServerConnection con = connector.getMBeanServerConnection();
			Set<?> objectInstances = con.queryMBeans(new ObjectName("f3-management:*"), null);
			
			Set<F3Runtime> f3Runtimes = new HashSet<F3Runtime>();
			
			for (Object obj : objectInstances) {
				ObjectInstance objectInstance = (ObjectInstance) obj;
				ObjectName objectName = objectInstance.getObjectName();
				String subDomain = objectName.getKeyProperty("SubDomain");
				f3Runtimes.add(new F3Runtime(subDomain, server));
			}
			
			return f3Runtimes;
			
		} finally {
			connector.close();
		}
		
	}

	/**
	 * Sets the JMX connection service.
	 * 
	 * @param jmxConnectionService JMX connection service.
	 */
	@Reference
	public void setJmxConnectionService(JmxConnectionService jmxConnectionService) {
		this.jmxConnectionService = jmxConnectionService;
	}

	/**
	 * Sets the MBean server name.
	 * 
	 * @param mbeanServer MBean server name.
	 */
	@Property
	public void setMbeanServer(String mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

}
