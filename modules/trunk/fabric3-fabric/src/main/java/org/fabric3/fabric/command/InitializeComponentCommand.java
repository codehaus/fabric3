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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.command.Command;

/**
 * A command to initialize a composite scoped component on a service node that is included in an activated composite.
 * For example, composite-scoped components included at the domain level.
 *
 * @version $Rev$ $Date$
 */
public class InitializeComponentCommand implements Command {
    public static final QName QNAME = new QName(Constants.FABRIC3_NS, "initializeComponentCommand");
    private final List<URI> uris;
    private final URI groupId;

    public InitializeComponentCommand(URI groupId) {
        this.groupId = groupId;
        uris = new ArrayList<URI>();
    }

    public List<URI> getUris() {
        return Collections.unmodifiableList(uris);
    }

    public void addUri(URI uri) {
        uris.add(uri);
    }

    public URI getGroupId() {
        return groupId;
    }
}
