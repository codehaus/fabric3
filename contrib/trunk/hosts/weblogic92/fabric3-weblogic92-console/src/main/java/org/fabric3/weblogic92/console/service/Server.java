package org.fabric3.weblogic92.console.service;

import java.util.Set;

/**
 * Represents a server within the WLS domain.
 * 
 * @author meerajk
 * 
 */
public class Server {

	private final String name;
	private final Integer port;
	private final String address;
	private final String state;
	private final Set<F3Runtime> f3Runtimes;

	/**
	 * Initializes the name, listen port, listen address and state of the server.
	 * 
	 * @param name Name of the server.
	 * @param port Listen port of the server.
	 * @param address Listen address of the server.
	 * @param state State of the server.
	 * @param f3Runtimes F3 runtimes configured on this server.
	 */
	public Server(String name, Integer port, String address, String state, Set<F3Runtime> f3Runtimes) {
		this.name = name;
		this.port = port;
		this.address = address;
		this.state = state;
		this.f3Runtimes = f3Runtimes;
	}

	/**
	 * Gets the state of the server.
	 * 
	 * @return State of the server.
	 */
	public String getState() {
		return state;
	}

	/**
	 * Gets tha name of the server.
	 * 
	 * @return Name of the server.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the listen port for the server.
	 * 
	 * @return Listen port for the server.
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * Gets the listen address for the server.
	 * 
	 * @return Listen address for the server.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Get the set of configured F3 runtimes on the server.
	 * 
	 * @return Configured F3 runtimes on the server.
	 */
	public Set<F3Runtime> getF3Runtimes() {
		return f3Runtimes;
	}

}
