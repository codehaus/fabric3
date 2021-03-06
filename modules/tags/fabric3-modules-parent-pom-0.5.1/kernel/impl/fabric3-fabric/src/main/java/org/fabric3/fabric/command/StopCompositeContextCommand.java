package org.fabric3.fabric.command;

import java.net.URI;

import org.fabric3.spi.command.AbstractCommand;

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
public class StopCompositeContextCommand extends AbstractCommand {

    private final URI groupId;

    public StopCompositeContextCommand(int order, URI groupId) {
        super(order);
        this.groupId = groupId;
        assert groupId != null;
    }

    public URI getGroupId() {
        return groupId;
    }

    public int hashCode() {
        return groupId.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        try {
            StopCompositeContextCommand other = (StopCompositeContextCommand) object;
            return groupId.equals(other.groupId);
        } catch (ClassCastException cce) {
            return false;
        }
    }
}
