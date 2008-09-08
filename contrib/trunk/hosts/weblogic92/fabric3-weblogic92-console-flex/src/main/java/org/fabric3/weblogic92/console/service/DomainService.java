package org.fabric3.weblogic92.console.service;

import java.io.IOException;

import javax.management.JMException;

/**
 * Interface for getting the all the servers in the runtime.
 * 
 * @author meerajk
 *
 */
public interface DomainService {

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
	Server[] getServers(String url, int port, String user, String password) throws IOException, JMException;

}
