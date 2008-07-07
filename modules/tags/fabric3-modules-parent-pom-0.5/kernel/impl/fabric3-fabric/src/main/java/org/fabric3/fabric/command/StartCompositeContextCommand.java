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

import java.net.URI;

import org.fabric3.spi.command.AbstractCommand;

/**
 * Starts a composite context on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class StartCompositeContextCommand extends AbstractCommand {
    private final URI groupId;

    public StartCompositeContextCommand(int order, URI groupId) {
        super(order);
        this.groupId = groupId;
    }

    public URI getGroupId() {
        return groupId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartCompositeContextCommand that = (StartCompositeContextCommand) o;

        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (groupId != null ? groupId.hashCode() : 0);
    }
}
