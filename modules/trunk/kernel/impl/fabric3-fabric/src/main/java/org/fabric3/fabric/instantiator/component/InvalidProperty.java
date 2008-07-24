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

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;

/**
 * @version $Rev: 4789 $ $Date: 2008-06-08 07:54:46 -0700 (Sun, 08 Jun 2008) $
 */
public class InvalidProperty extends AssemblyFailure {
    private String name;
    private Throwable cause;

    public InvalidProperty(URI componentURI, String name, Throwable cause) {
        super(componentURI);
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
        return "The property " + name + " in component " + getComponentUri() + "is invalid " + ". The error thrown was: \n" + cause;
    }
}