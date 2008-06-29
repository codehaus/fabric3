package org.fabric3.binding.ws.mq.scdl;

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.scdl.BindingDefinition;
import org.osoa.sca.Constants;

public class MqBindingDefinition extends BindingDefinition {
	
	private static final long serialVersionUID = 2043799331585239948L;

	private static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.ws.mq");
	private final String destination;
	private final URI host;
	
	/**
	 * Initialise by the given destination and hosts
	 * @param destination
	 * @param host
	 */
	public MqBindingDefinition(String destination, URI host) {
		super(BINDING_QNAME);
		this.destination = destination;
		this.host = host;
	}

	/**
	 * Return the destination of where the message will go
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * Return the host where of the MQ server
	 * @return the host
	 */
	public URI getHost() {
		return host;
	}
}
