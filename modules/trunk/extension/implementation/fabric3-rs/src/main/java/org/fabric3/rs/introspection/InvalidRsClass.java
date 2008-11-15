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
package org.fabric3.rs.introspection;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import org.fabric3.scdl.ValidationFailure;

/**
 * @version $Rev$ $Date$
 */
public class InvalidRsClass extends ValidationFailure<Class<?>> {

    public InvalidRsClass(Class<?> implClass) {
        super(implClass);
    }

    @Override
    public String getMessage() {
        return String.format("Implementation class %s is not annotated with REST annotation %s or %s ", getValidatable().getName(), Path.class.getName(), Provider.class.getName());
    }
}
