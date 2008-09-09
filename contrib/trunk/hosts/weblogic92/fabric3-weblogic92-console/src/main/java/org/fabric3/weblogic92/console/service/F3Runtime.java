package org.fabric3.weblogic92.console.service;

/**
 * Represents an F3 runtime within a server.
 * 
 * @author meerajk
 *
 */
public class F3Runtime {
	
	private final String subDomain;
	
	/**
	 * Sets the runtime subdomain and server.
	 * 
	 * @param subDomain Runtime subdomain.
	 */
	public F3Runtime(String subDomain) {
		this.subDomain = subDomain;
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
