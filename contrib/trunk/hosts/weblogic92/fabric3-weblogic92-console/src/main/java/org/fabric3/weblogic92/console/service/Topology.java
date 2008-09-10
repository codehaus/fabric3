package org.fabric3.weblogic92.console.service;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Fabric3 runtime topology for the WLS domain.
 * 
 * @author meerajk
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("unused")
public class Topology {

	@XmlAttribute private String name = "Fabric3 Runtime Topology";
	@XmlElement(name = "server") private Set<Server> servers;
	
	/**
	 * Default constructor.
	 */
	public Topology() {
	}

	/**
	 * Initializes the set of servers.
	 * 
	 * @param servers Set of servers in the domain.
	 */
	public Topology(Set<Server> servers) {
		this.servers = servers;
	}

}
