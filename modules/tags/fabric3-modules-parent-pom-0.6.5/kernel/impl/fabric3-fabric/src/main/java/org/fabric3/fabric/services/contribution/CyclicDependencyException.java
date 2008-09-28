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
package org.fabric3.fabric.services.contribution;

import java.util.List;

import org.fabric3.fabric.util.graph.Cycle;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;

/**
 * Denotes a cyclic dependency between two or more contributions.
 *
 * @version $Rev$ $Date$
 */
public class CyclicDependencyException extends ContributionException {
    private static final long serialVersionUID = 3763877232188058275L;
    private final List<Cycle<Contribution>> cycles;

    public CyclicDependencyException(List<Cycle<Contribution>> cycles) {
        super("Cyclic dependency found", (String) null);
        this.cycles = cycles;
    }

    public List<Cycle<Contribution>> getCycles() {
        return cycles;
    }
}
