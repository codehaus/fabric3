package org.fabric3.weblogic92.console.service;

import java.io.IOException;
import java.util.Set;

import javax.management.JMException;

/**
 * Service to get the F3 runtimes within a server.
 * 
 * @author meerajk
 *
 */
public interface F3RuntimeService {
	
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
	Set<F3Runtime> getF3Runtimes(Server server, String user, String password) throws IOException, JMException;

}
