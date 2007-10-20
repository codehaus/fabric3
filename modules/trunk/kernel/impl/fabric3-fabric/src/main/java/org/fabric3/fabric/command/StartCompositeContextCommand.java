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
import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.command.Command;

/**
 * Starts a context with the composite container
 *
 * @version $Rev$ $Date$
 */
public class StartCompositeContextCommand implements Command {
    public static final QName QNAME = new QName(Constants.FABRIC3_NS, "startCompositeContextCommand");

    private final URI groupId;

    public StartCompositeContextCommand(URI groupId) {
        this.groupId = groupId;
    }

    public URI getGroupId() {
        return groupId;
    }
}
