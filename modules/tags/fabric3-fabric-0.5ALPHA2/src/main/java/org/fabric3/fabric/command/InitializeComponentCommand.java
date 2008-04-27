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
package org.fabric3.fabric.command;

import org.fabric3.spi.command.AbstractCommand;

/**
 * A command to initialize a composite scoped component on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class InitializeComponentCommand extends AbstractCommand {
    private final ComponentInitializationUri uri;


    public InitializeComponentCommand(int order, ComponentInitializationUri uri) {
        super(order);
        this.uri = uri;
    }

    public ComponentInitializationUri getUri() {
        return uri;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InitializeComponentCommand that = (InitializeComponentCommand) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (uri != null ? uri.hashCode() : 0);
    }
}
