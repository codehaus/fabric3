package org.fabric3.tests.function.ordering;

import org.osoa.sca.annotations.Property;

public class ItemImpl implements Item {
	private String typeName;
	
	public ItemImpl(@Property(name="typeName") String typeName) {
		this.typeName = typeName;
	}
	
	public String getName() {
		return typeName;
	}
}
