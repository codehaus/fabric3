package org.fabric3.weblogic92.console.service;

import java.io.IOException;

import javax.management.JMException;

/**
 * Service for getting the domain topology.
 * 
 * @author meerajk
 *
 */
public interface DomainTopologyService {
	
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
	Server[] getDomainTopology(String url, int port, String user, String password) throws IOException, JMException;

}
