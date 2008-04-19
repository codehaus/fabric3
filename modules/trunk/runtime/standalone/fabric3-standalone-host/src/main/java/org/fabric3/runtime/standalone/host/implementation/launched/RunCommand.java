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
package org.fabric3.runtime.standalone.host.implementation.launched;

import java.net.URI;

import org.fabric3.spi.command.AbstractCommand;

/**
 * Runs <code>implementation.launched</code> components on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class RunCommand extends AbstractCommand {
    private final URI uri;

    public RunCommand(int order, URI uri) {
        super(order);
        this.uri = uri;
    }

    public URI getComponentUri() {
        return uri;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RunCommand that = (RunCommand) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (uri != null ? uri.hashCode() : 0);
    }
}
