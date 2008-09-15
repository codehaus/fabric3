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

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;

/**
 * Processes contribution dependencies
 *
 * @version $Rev$ $Date$
 */
public interface DependencyService {

    /**
     * Orders a list of contributions by their import dependencies using a reverse topological sort of contribution
     * imports.
     *
     * @param contributions the  list of contributions to order
     * @return the ordered list of contributions
     * @throws ContributionException if an error occurs ordering the contributions such as an unresolvable import or
     *                               dependency cycle
     */
    List<Contribution> order(List<Contribution> contributions) throws ContributionException;

}
