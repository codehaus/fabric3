package org.fabric3.weblogic92.console.service;

import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Default implementation of the domain service.
 * 
 * @author meerajk
 *
 */
public class DefaultDomainService implements DomainService {
	
	private JmxConnectionService jmxConnectionService;
	private String mbeanServer;
	private ObjectName domainRuntimeServiceMBeanName;

	/**
	 * Gets all the servers currently in the domain.
	 * 
	 * @param url URL for the remote server.
	 * @param port Port for the remote server.
	 * @param user User to connect to the server.
	 * @param password Password to connect to the server.
	 * 
	 * @return All servers currently in the domain.
	 * @throws IOException If unable to get a JMX connection.
	 * @throws JMException In case of any JMX errors.
	 */
	public Server[] getServers(String url, int port, String user, String password) throws IOException, JMException {
		
		JMXConnector connector = jmxConnectionService.getConnector(url, port, mbeanServer, user, password);
		
		try {
			
			MBeanServerConnection connection = connector.getMBeanServerConnection();
			
			ObjectName[] serverNames = (ObjectName[]) connection.getAttribute(domainRuntimeServiceMBeanName, "ServerRuntimes");
			Server[] servers = new Server[serverNames.length];
			
			for (int i = 0;i < serverNames.length;i++) {
				
				ObjectName serverName = serverNames[i];
				
				String name = (String) connection.getAttribute(serverName, "Name");
				int listenPort = (Integer) connection.getAttribute(serverName, "ListenPort");
				String listenAddress = (String) connection.getAttribute(serverName, "ListenAddress");
				String state = (String) connection.getAttribute(serverName, "State");
				
				servers[i] = new Server(name, listenPort, listenAddress, state);
				
			}
			
			return servers;
			
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

	/**
	 * Sets the domain runtime service MBean name.
	 * 
	 * @param domainRuntimeServiceMBeanName Domain runtime service MBean name.
	 */
	@Property
	public void setDomainRuntimeServiceMBeanName(ObjectName domainRuntimeServiceMBeanName) {
		this.domainRuntimeServiceMBeanName = domainRuntimeServiceMBeanName;
	}

}
