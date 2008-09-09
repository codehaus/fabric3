package org.fabric3.weblogic92.console.service;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a server within the WLS domain.
 * 
 * @author meerajk
 * 
 */
@XmlRootElement
public class Server {

	private String name;
	private Integer port;
	private String address;
	private String state;
	private Set<String> f3Runtimes;
	
	public Server() {
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
	 * Gets the set of configured F3 runtimes on the server.
	 * 
	 * @return Configured F3 runtimes on the server.
	 */
	public Set<String> getF3Runtimes() {
		return f3Runtimes;
	}

	/**
	 * Sets the name of the server.
	 * 
	 * @param name Name of the server.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the listen port of the server.
	 * 
	 * @param port Listen port of the server.
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * Sets the listen address of the server.
	 * 
	 * @param address Listen address of the server.
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Sets the state of the server.
	 * 
	 * @param state State of the server.
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Sets the set of configured F3 runtimes on the server.
	 * 
	 * @param runtimes Configured F3 runtimes on the server.
	 */
	public void setF3Runtimes(Set<String> runtimes) {
		f3Runtimes = runtimes;
	}

}
