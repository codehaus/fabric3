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
package org.fabric3.fabric.instantiator;

import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.spi.model.instance.Bindable;

public class AmbiguousService extends AssemblyFailure {
    private Bindable bindable;
    private String message;

    /**
     * Constructor.
     *
     * @param bindable the logical service or reference that is invalid
     * @param message  the error message
     */
    public AmbiguousService(Bindable bindable, String message) {
        super(bindable.getParent().getUri());
        this.bindable = bindable;
        this.message = message;
    }

    public Bindable getBindable() {
        return bindable;
    }

    public String getMessage() {
        return message;
    }

}
