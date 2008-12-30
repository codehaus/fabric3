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
 * @version $Rev$ $Date$
 */
public class InvalidPropertyFile extends AssemblyFailure {
    private String name;
    private Throwable cause;
    private final URI file;

    public InvalidPropertyFile(String name, Throwable cause, URI file, URI componentUri, URI contributionUri) {
        super(componentUri, contributionUri);
        this.name = name;
        this.cause = cause;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public Throwable getCause() {
        return cause;
    }

    public URI getFile() {
        return file;
    }

    public String getMessage() {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        cause.printStackTrace(pw);
        return "The property file for property " + name + " in component " + getComponentUri()
                + " is invalid due to an error processing the file  " + file + ". The error thrown was: \n" + writer;
    }
}
