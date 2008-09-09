package org.fabric3.weblogic92.console.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an F3 runtime.
 * 
 * @author meerajk
 *
 */
@XmlRootElement
@SuppressWarnings("unused")
public class F3Runtime {
	
	@XmlAttribute
	private String name;
	
	/**
	 * @param name Name (management sub-domain) of the F3 runtime.
	 */
	public F3Runtime(String name) {
		this.name = name;
	}

	/**
	 * Equality based on name.
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof F3Runtime && ((F3Runtime) obj).name.equals(name);
	}

	/**
	 * Hashcode based on name.
	 */
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	/**
	 * Default constructor.
	 */
	public F3Runtime() {
	}

}
