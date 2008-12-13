/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.util.graph;

import java.util.List;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class TopologicalSortTestCase extends TestCase {

    public void testMultiLevelSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        Edge<String> edgeBC = new EdgeImpl<String>(b, c);
        graph.add(edgeBC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().sort(graph);
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(c, list.get(2));
    }

    public void testMultiLevelReverseSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        Edge<String> edgeBC = new EdgeImpl<String>(b, c);
        graph.add(edgeBC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().reverseSort(graph);
        assertEquals(c, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(a, list.get(2));
    }

    public void testReverseSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().reverseSort(graph);
        assertEquals(a, list.get(2));
        assertTrue(list.contains(c));
        assertTrue(list.contains(b));
    }

    public void testSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().sort(graph);
        assertEquals(a, list.get(0));
        assertTrue(list.contains(b));
        assertTrue(list.contains(c));
    }

}
