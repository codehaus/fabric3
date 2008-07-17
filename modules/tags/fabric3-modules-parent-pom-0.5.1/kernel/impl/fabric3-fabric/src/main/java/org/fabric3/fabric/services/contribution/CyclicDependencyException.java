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
