package org.fabric3.spi.model.instance;

/**
 * Represents the type of the wire. WIres are either ecplictly requested 
 * using the wire element in the composite, result of an autowire or 
 * result of a requested target on a reference;
 * 
 * @version $Revision$ $Date$
 *
 */
public enum WireType {
    
    EXPLICIT, AUTOWIRED, TARGETED;

}
