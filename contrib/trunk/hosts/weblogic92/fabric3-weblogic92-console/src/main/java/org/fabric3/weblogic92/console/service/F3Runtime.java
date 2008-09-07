package org.fabric3.weblogic92.console.service;

/**
 * Represents an F3 runtime within a server.
 * 
 * @author meerajk
 *
 */
public class F3Runtime {
	
	private final String subDomain;
	private final Server server;
	
	/**
	 * Sets the runtime subdomain and server.
	 * 
	 * @param subDomain Runtime subdomain.
	 * @param server Server that hosts the runtime.
	 */
	public F3Runtime(String subDomain, Server server) {
		this.subDomain = subDomain;
		this.server = server;
	}

	/**
	 * Gets the sub-domain for the runtime.
	 * 
	 * @return Sub domain for the runtime.
	 */
	public String getSubDomain() {
		return subDomain;
	}

	/**
	 * Gets the server that hosts the runtime.
	 * 
	 * @return Server that hosts the runtime.
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Equality based on sub-domain.
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof F3Runtime && subDomain.equals(((F3Runtime) obj).subDomain);
	}

	/**
	 * Hashcode based on sub-domain.
	 */
	@Override
	public int hashCode() {
		return subDomain.hashCode();
	}

}
