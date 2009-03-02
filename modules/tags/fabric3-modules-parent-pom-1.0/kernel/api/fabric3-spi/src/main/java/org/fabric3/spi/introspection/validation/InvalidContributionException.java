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
package org.fabric3.spi.introspection.validation;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class InvalidContributionException extends ValidationException {
    private static final long serialVersionUID = -5729273092766880963L;

    /**
     * Constructor.
     *
     * @param errors   the errors that were found during validation
     * @param warnings the warnings that were found during validation
     */
    public InvalidContributionException(List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super(errors, warnings);
    }

    /**
     * Constructor.
     *
     * @param errors the errors that were found during validation
     */
    public InvalidContributionException(List<ValidationFailure> errors) {
        super(errors, new ArrayList<ValidationFailure>());
    }

    public String getMessage() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        if (!getErrors().isEmpty()) {
            ValidationUtils.writeErrors(writer, getErrors());
            writer.write("\n");
        }
        if (!getWarnings().isEmpty()) {
            ValidationUtils.writeWarnings(writer, getWarnings());
        }
        return bas.toString();
    }

}
