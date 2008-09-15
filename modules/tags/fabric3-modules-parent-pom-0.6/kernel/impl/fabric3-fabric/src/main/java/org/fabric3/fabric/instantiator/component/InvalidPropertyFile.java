/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public InvalidPropertyFile(URI componentURI, String name, Throwable cause, URI file) {
        super(componentURI);
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
