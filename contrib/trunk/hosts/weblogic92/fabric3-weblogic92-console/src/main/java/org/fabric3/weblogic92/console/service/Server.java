package org.fabric3.weblogic92.console.service;

/**
 * Represents a server within the WLS domain.
 * 
 * @author meerajk
 * 
 */
public class Server {

	private final String name;
	private final Integer listenPort;
	private final String listenAddress;
	private final String state;

	/**
	 * Initializes the name, listen port, listen address and state of the server.
	 * 
	 * @param name Name of the server.
	 * @param listenPort Listen port of the server.
	 * @param listenAddress Listen address of the server.
	 * @param state State of the server.
	 */
	public Server(String name, Integer listenPort, String listenAddress, String state) {
		this.name = name;
		this.listenPort = listenPort;
		this.listenAddress = listenAddress;
		this.state = state;
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
	public Integer getListenPort() {
		return listenPort;
	}

	/**
	 * Gets the listen address for the server.
	 * 
	 * @return Listen address for the server.
	 */
	public String getListenAddress() {
		return listenAddress;
	}

}
