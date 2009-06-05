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

/**
 * A interface for a visitor in the Visitor Pattern.
 *
 * @version $Rev$ $Date$
 */
public interface Visitor<T> {

    /**
     * Perform the visit staring at the given vertex
     *
     * @param start the starting vertex
     * @return true if completed successfully
     */
    public boolean visit(Vertex<T> start);


}

