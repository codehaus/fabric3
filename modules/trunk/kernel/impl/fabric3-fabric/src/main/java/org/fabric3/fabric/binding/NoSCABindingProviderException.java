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
package org.fabric3.fabric.binding;

import java.util.List;

import org.fabric3.spi.binding.BindingMatchResult;
import org.fabric3.spi.binding.BindingSelectionException;

/**
 * @version $Revision$ $Date$
 */
public class NoSCABindingProviderException extends BindingSelectionException {
    private static final long serialVersionUID = -7797860974206005955L;
    private List<BindingMatchResult> results;

    public NoSCABindingProviderException(String message, List<BindingMatchResult> results) {
        super(message);
        this.results = results;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());
        builder.append("\nThe SCA binding selectors reported the following:\n");
        for (BindingMatchResult result : results) {
            builder.append(result.getType()).append("\n");
            for (String reason : result.getReasons()) {
                builder.append("  ").append(reason).append("\n");
            }

        }
        return builder.toString();
    }

}
