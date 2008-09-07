package org.fabric3.weblogic92.console.service;

import java.io.IOException;

import javax.management.remote.JMXConnector;

/**
 * Service for getting managed JMX connections.
 * 
 * @author meerajk
 *
 */
public interface JmxConnectionService {
	
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
	JMXConnector getConnector(String url, int port, String mbeanServer, String user, String password) throws IOException;

}
