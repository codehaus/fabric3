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
 * Default implementation of the domain topology service.
 * 
 * @author meerajk
 *
 */
public class DefaultDomainTopologyService implements DomainTopologyService {
	
	private JmxConnectionService jmxConnectionService;
	private String domainServer;
	private String runtimeServer;
	private ObjectName domainRuntimeServiceMBeanName;
	
	/**
	 * Gets the F3 runtime topology for the weblogic domain.
	 * 
	 * @param url Listen address of the admin server.
	 * @param port Listen port of the admin server.
	 * @param user Admin user for the server.
	 * @param password Admin password for the server.
	 * @return List of servers in the domain, with hosted F3 runtimes.
	 * 
	 * @throws IOException If unable to connect to the admin server.
	 * @throws JMException In case of any unexpected JMX exception.
	 */
	public Server[] getDomainTopology(String url, int port, String user, String password) throws IOException, JMException {
		
		JMXConnector connector = jmxConnectionService.getConnector(url, port, domainServer, user, password);
		
		try {
			
			MBeanServerConnection connection = connector.getMBeanServerConnection();
			
			ObjectName[] serverNames = (ObjectName[]) connection.getAttribute(domainRuntimeServiceMBeanName, "ServerRuntimes");
			Server[] servers = new Server[serverNames.length];
			
			for (int i = 0;i < serverNames.length;i++) {
				
				ObjectName serverName = serverNames[i];
				
				String name = (String) connection.getAttribute(serverName, "Name");
				int listenPort = (Integer) connection.getAttribute(serverName, "ListenPort");
				String listenAddress = (String) connection.getAttribute(serverName, "ListenAddress");
				listenAddress = listenAddress.substring(1);
				String state = (String) connection.getAttribute(serverName, "State");
				
				Set<F3Runtime> f3Runtimes = getF3Runtimes(listenAddress, listenPort, user, password);
				
				servers[i] = new Server(name, listenPort, listenAddress.substring(1), state, f3Runtimes);
				
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
	 * Sets the domain MBean server name.
	 * 
	 * @param domainServer Domain MBean server name.
	 */
	@Property
	public void setDomainServer(String domainServer) {
		this.domainServer = domainServer;
	}

	/**
	 * Sets the runtime MBean server name.
	 * 
	 * @param runtimeServer Runtime MBean server name.
	 */
	@Property
	public void setRuntimeServer(String runtimeServer) {
		this.runtimeServer = runtimeServer;
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
	
	/*
	 * Gets the configured runtime on a managed server.
	 */
	private Set<F3Runtime> getF3Runtimes(String url, int port, String user, String password) throws IOException, JMException {
		
		JMXConnector connector = jmxConnectionService.getConnector(url, port, runtimeServer, user, password);
	
		try {
			
			MBeanServerConnection con = connector.getMBeanServerConnection();
			Set<?> objectInstances = con.queryMBeans(new ObjectName("f3-management:*"), null);
			
			Set<F3Runtime> f3Runtimes = new HashSet<F3Runtime>();
			
			for (Object obj : objectInstances) {
				ObjectInstance objectInstance = (ObjectInstance) obj;
				ObjectName objectName = objectInstance.getObjectName();
				String subDomain = objectName.getKeyProperty("SubDomain");
				f3Runtimes.add(new F3Runtime(subDomain));
			}
			
			return f3Runtimes;
			
		} finally {
			connector.close();
		}
		
	}

}
