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
package org.fabric3.spi.model.instance;

import org.fabric3.model.type.service.Operation;

/**
 * Represents an operation on a service, reference or resource of an instantiated component.
 *
 * @version $Revision$ $Date$
 */
public class LogicalOperation extends LogicalScaArtifact<LogicalAttachPoint> {
    private static final long serialVersionUID = -3846488579836419406L;
    private Operation definition;

    /**
     * Constructor.
     *
     * @param definition the operation definition
     * @param parent     Parent of the SCA artifact
     */
    public LogicalOperation(Operation definition, LogicalAttachPoint parent) {
        super(null, parent, null);
        this.definition = definition;
        addIntents(definition.getIntents());
        addPolicySets(definition.getPolicySets());
    }

    public Operation getDefinition() {
        return definition;
    }
}
