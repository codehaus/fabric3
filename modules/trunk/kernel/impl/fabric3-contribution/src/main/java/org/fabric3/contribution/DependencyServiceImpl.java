/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.contribution;

import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.util.graph.Cycle;
import org.fabric3.util.graph.CycleDetector;
import org.fabric3.util.graph.CycleDetectorImpl;
import org.fabric3.util.graph.DirectedGraph;
import org.fabric3.util.graph.DirectedGraphImpl;
import org.fabric3.util.graph.Edge;
import org.fabric3.util.graph.EdgeImpl;
import org.fabric3.util.graph.GraphException;
import org.fabric3.util.graph.TopologicalSorter;
import org.fabric3.util.graph.TopologicalSorterImpl;
import org.fabric3.util.graph.Vertex;
import org.fabric3.util.graph.VertexImpl;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of the DependencyService
 *
 * @version $Rev$ $Date$
 */
public class DependencyServiceImpl implements DependencyService {
    private CycleDetector<Contribution> detector;
    private TopologicalSorter<Contribution> sorter;
    private MetaDataStore store;


    public DependencyServiceImpl(@Reference MetaDataStore store) {
        this.store = store;
        detector = new CycleDetectorImpl<Contribution>();
        sorter = new TopologicalSorterImpl<Contribution>();
    }

    public List<Contribution> order(List<Contribution> contributions) throws DependencyException {
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
            for (Import imprt : manifest.getImports()) {
                // first, see if the import is already installed
                // note that extension imports do not need to be checked since we assume extensons are installed prior
                if (store.isResolved(imprt)) {
                    continue;
                }
                Vertex<Contribution> sink = findTargetVertex(dag, imprt);
                if (sink == null) {
                    String uri = contribution.getUri().toString();
                    throw new UnresolvableImportException("Unable to resolve import " + imprt + " in contribution " + uri, imprt);
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
            throw new DependencyException(e);
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

}
