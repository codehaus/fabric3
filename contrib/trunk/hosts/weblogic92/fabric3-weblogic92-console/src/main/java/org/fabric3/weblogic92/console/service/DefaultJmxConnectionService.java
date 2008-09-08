package org.fabric3.weblogic92.console.service;

import java.io.IOException;
import java.util.Hashtable;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

/**
 * Default implementation of the JMX connection service.
 * 
 * @author meerajk
 *
 */
public class DefaultJmxConnectionService implements JmxConnectionService {
	
	/**
	 * Gets a remote JMX connector.
	 * 
	 * @param url URL for the remote server.
	 * @param port Port for the remote server.
	 * @param mbeanServer JNDI name of the MBean server.
	 * @param user User to connect to the server.
	 * @param password Password to connect to the server.
	 * 
	 * @return An instance of the remote JMX connector.
	 * @throws IOException If unable to establish a connection.
	 */
	public JMXConnector getConnector(String url, int port, String mbeanServer, String user, String password) throws IOException {
		
		String jndiroot= "/jndi/iiop://" + url + ":" + port + "/";

		JMXServiceURL serviceURL = new JMXServiceURL("rmi", url, port, jndiroot + mbeanServer);


		Hashtable<String, Object> h = new Hashtable<String, Object>();
		h.put(Context.SECURITY_PRINCIPAL, user);
		h.put(Context.SECURITY_CREDENTIALS, password);

		return JMXConnectorFactory.connect(serviceURL, h);
		
	}

}
