package org.fabric3.binding.ws.mq.runtime;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

public class MqWireSourceAttacher implements SourceWireAttacher {


	/** 
	 * 
	 */
	public void attachToSource(PhysicalWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition wireTargetDefinition, Wire wire) throws WiringException {
         throw new AssertionError("Not Implemented");
	}

	/**
	 * 
	 */
	public void detachFromSource(PhysicalWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition wireTargetDefinition, Wire wire) throws WiringException {
         throw new AssertionError("Not Implemented");
	}
	
	/* 
	 * 
	 */
	public void attachObjectFactory(PhysicalWireSourceDefinition sourceDefinition, ObjectFactory factory, PhysicalWireTargetDefinition wireTargetDefinition) throws WiringException {
		throw new AssertionError("Not Supported");
	}


}
