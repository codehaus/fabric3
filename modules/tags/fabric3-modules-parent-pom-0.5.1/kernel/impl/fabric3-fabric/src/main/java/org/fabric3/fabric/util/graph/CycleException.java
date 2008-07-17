package org.fabric3.fabric.util.graph;

/**
 * Thrown when a cycle in a DAG is encountered.
 *
 * @version $Rev$ $Date$
 */

public class CycleException extends GraphException {
    private static final long serialVersionUID = 5516809158764076723L;

    public CycleException() {
        super("Cycle detected");
    }

}