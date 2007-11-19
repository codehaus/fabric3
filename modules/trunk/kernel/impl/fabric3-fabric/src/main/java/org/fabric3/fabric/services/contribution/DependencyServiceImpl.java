/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.services.contribution;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.util.graph.Cycle;
import org.fabric3.fabric.util.graph.CycleDetector;
import org.fabric3.fabric.util.graph.CycleDetectorImpl;
import org.fabric3.fabric.util.graph.DirectedGraph;
import org.fabric3.fabric.util.graph.DirectedGraphImpl;
import org.fabric3.fabric.util.graph.Edge;
import org.fabric3.fabric.util.graph.EdgeImpl;
import org.fabric3.fabric.util.graph.GraphException;
import org.fabric3.fabric.util.graph.TopologicalSorter;
import org.fabric3.fabric.util.graph.TopologicalSorterImpl;
import org.fabric3.fabric.util.graph.Vertex;
import org.fabric3.fabric.util.graph.VertexImpl;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of the DependencyService
 *
 * @version $Rev$ $Date$
 */
public class DependencyServiceImpl implements DependencyService {
    private ContributionStoreRegistry storeRegistry;
    private CycleDetector<Contribution> detector;
    private TopologicalSorter<Contribution> sorter;
    private String uriPrefix = "file://contribution/";


    public DependencyServiceImpl(@Reference ContributionStoreRegistry storeRegistry) {
        this.storeRegistry = storeRegistry;
        detector = new CycleDetectorImpl<Contribution>();
        sorter = new TopologicalSorterImpl<Contribution>();
    }

    public List<Contribution> order(List<Contribution> contributions) throws ContributionException {
        // create a DAG
        DirectedGraph<Contribution> dag = new DirectedGraphImpl<Contribution>();
        // add the contributions as vertices
        for (Contribution contribution : contributions) {
            dag.add(new VertexImpl<Contribution>(contribution));
        }
        // add edges based on imports
        for (Vertex<Contribution> source : dag.getVertices()) {
            Contribution contribution = source.getEntity();
            ContributionManifest manifest = contribution.getManifest();
            assert manifest != null;
            MetaDataStore store = storeRegistry.getMetadataStore(parseStoreId(contribution.getUri()));
            assert store != null;
            for (Import imprt : manifest.getImports()) {
                // xcv FIXME move import resolution to here
                // first, see if the import is already installed

                if (store.resolve(imprt) != null) {
                    continue;
                }
                Vertex<Contribution> sink = findTargetVertex(dag, imprt);
                if (sink == null) {
                    String uri = contribution.getUri().toString();
                    throw new UnresolvableImportException("Import unable to be resolved", uri, imprt);
                }
                Edge<Contribution> edge = new EdgeImpl<Contribution>(source, sink);
                dag.add(edge);
            }

        }
        // detect cycles
        List<Cycle<Contribution>> cycles = detector.findCycles(dag);
        if (!cycles.isEmpty()) {
            // cycles were detected
            throw new CyclicDependencyException(cycles);
        }
        try {
            List<Vertex<Contribution>> vertices = sorter.reverseSort(dag);
            List<Contribution> ordered = new ArrayList<Contribution>(vertices.size());
            for (Vertex<Contribution> vertex : vertices) {
                ordered.add(vertex.getEntity());
            }
            return ordered;
        } catch (GraphException e) {
            throw new ContributionException(e);
        }
    }

    /**
     * Finds the Vertex in the graph with a maching export
     *
     * @param dag   the graph to resolve against
     * @param imprt the import to resolve
     * @return the matching Vertext or null
     */
    private Vertex<Contribution> findTargetVertex(DirectedGraph<Contribution> dag, Import imprt) {
        for (Vertex<Contribution> vertex : dag.getVertices()) {
            Contribution contribution = vertex.getEntity();
            ContributionManifest manifest = contribution.getManifest();
            assert manifest != null;
            for (Export export : manifest.getExports()) {
                if (Export.EXACT_MATCH == export.match(imprt)) {
                    return vertex;
                }
            }
        }
        return null;
    }

    /**
     * FIXME remove this method when the runtime is converted to a single store
     */
    private String parseStoreId(URI uri) {
        String s = uri.toString();
        assert s.length() > uriPrefix.length();
        s = s.substring(uriPrefix.length());
        int index = s.indexOf("/");
        if (index > 0) {
            return s.substring(0, index);
        } else {
            return s;
        }

    }

}
