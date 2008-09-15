package org.fabric3.fabric.util.graph;

import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class CycleTestCase extends TestCase {

    public void testCycle() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Edge<String> edgeBA = new EdgeImpl<String>(b, a);
        graph.add(edgeBA);
        CycleDetector<String> detector = new CycleDetectorImpl<String>();
        assertTrue(detector.hasCycles(graph));
        DirectedGraph<String> dg = detector.findCycleSubgraph(graph);
        Set<Edge<String>> edges = dg.getEdges();
        assertTrue(edges.contains(edgeAB));
        assertTrue(edges.contains(edgeBA));
    }
}
