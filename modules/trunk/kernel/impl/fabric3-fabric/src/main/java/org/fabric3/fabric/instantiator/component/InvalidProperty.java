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
package org.fabric3.fabric.instantiator.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;

/**
 * @version $Rev: 4789 $ $Date: 2008-06-08 07:54:46 -0700 (Sun, 08 Jun 2008) $
 */
public class InvalidProperty extends AssemblyFailure {
    private String name;
    private Throwable cause;

    public InvalidProperty(String name, Throwable cause, URI componentUri, URI contributionUri) {
        super(componentUri, contributionUri);
        this.name = name;
        this.cause = cause;
    }

    public String getName() {
        return name;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        cause.printStackTrace(pw);
        return "The property " + name + " in component " + getComponentUri() + " is invalid " + ". The error thrown was: \n" + writer;
    }
}