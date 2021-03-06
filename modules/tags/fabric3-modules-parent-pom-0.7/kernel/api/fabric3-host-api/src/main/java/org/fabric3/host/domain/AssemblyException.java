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
package org.fabric3.host.domain;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Denotes a recoverable failure updating the domain assembly during deployment. For example, a failure may be a reference targeted to a non-existent
 * service.
 *
 * @version $Revision$ $Date$
 */
public class AssemblyException extends DeploymentException {
    private static final long serialVersionUID = 3957908169593535300L;
    private static final Comparator<AssemblyFailure> COMPARATOR = new Comparator<AssemblyFailure>() {
        public int compare(AssemblyFailure first, AssemblyFailure second) {
            return first.getComponentUri().compareTo(second.getComponentUri());
        }
    };

    private final List<AssemblyFailure> errors;

    public AssemblyException(List<AssemblyFailure> errors) {
        this.errors = errors;
    }

    public List<AssemblyFailure> getErrors() {
        return errors;
    }

    public String getMessage() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);

        if (!errors.isEmpty()) {
            List<AssemblyFailure> sorted = new ArrayList<AssemblyFailure>(errors);
            // sort the errors by component
            Collections.sort(sorted, COMPARATOR);
            for (AssemblyFailure error : sorted) {
                writer.write(error.getMessage());
                writer.write("\n\n");
            }
        }
        writer.flush();
        return bas.toString();

    }
}
