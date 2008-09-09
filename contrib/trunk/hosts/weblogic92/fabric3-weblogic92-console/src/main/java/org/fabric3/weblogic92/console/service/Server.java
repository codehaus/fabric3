package org.fabric3.weblogic92.console.service;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a server within the WLS domain.
 * 
 * @author meerajk
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("unused")
public class Server {

	@XmlAttribute private String name;
	@XmlAttribute private int port;
	@XmlAttribute private String address;
	@XmlAttribute private String state;
	@XmlElement(name = "f3Runtime")
	private Set<F3Runtime> f3Runtimes;
	
	public Server() {
	}

	public Server(String name, int port, String address, String state, Set<F3Runtime> runtimes) {
		this.name = name;
		this.port = port;
		this.address = address;
		this.state = state;
		f3Runtimes = runtimes;
	}

}
