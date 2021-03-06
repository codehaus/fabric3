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

import java.util.List;

import org.fabric3.spi.contribution.Contribution;

/**
 * Processes contribution dependencies
 *
 * @version $Rev: 6228 $ $Date: 2008-12-13 00:24:14 -0800 (Sat, 13 Dec 2008) $
 */
public interface DependencyService {

    /**
     * Orders a list of contributions by their import dependencies using a reverse topological sort of contribution imports.
     *
     * @param contributions the  list of contributions to order
     * @return the ordered list of contributions
     * @throws DependencyException if an error occurs ordering the contributions such as an unresolvable import or dependency cycle
     */
    List<Contribution> order(List<Contribution> contributions) throws DependencyException;

    /**
     * Orders a list of contributions to uninsall. Ordering is calculated by topologically sorting the list based on contribution imports.
     *
     * @param contributions the contributions to order
     * @return the ordered list of contributions
     */
    List<Contribution> orderForUninstall(List<Contribution> contributions);

}
