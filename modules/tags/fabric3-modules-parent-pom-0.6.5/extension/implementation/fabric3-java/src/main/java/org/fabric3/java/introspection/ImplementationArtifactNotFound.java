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
package org.fabric3.java.introspection;

import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.java.scdl.JavaImplementation;

/**
 * @version $Revision$ $Date$
 */
public class ImplementationArtifactNotFound extends ValidationFailure<JavaImplementation> {
    private String artifact;

    public ImplementationArtifactNotFound(JavaImplementation implementation) {
        super(implementation);
    }

    public ImplementationArtifactNotFound(JavaImplementation implementation, String artifact) {
        super(implementation);
        this.artifact = artifact.replace("/", ".");
    }

    public String getMessage() {
        String implementationClass = getValidatable().getImplementationClass();
        if (artifact == null || artifact.equals(implementationClass)) {
            return "Implementation class not found: " + implementationClass + ". Check that the class is contained in the contribution archive, " +
                    "included as a library, or imported in the SCA contribution manifest.";
        } else {
            return "Class " + artifact + " referenced in component implementation " + implementationClass + " not found. Check that the class is " +
                    "contained in the contribution archive, included as a library, or imported in the SCA contribution manifest.";
        }
    }
}
