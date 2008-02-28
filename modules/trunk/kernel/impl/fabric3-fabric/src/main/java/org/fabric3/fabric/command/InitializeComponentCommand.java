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
import org.fabric3.spi.command.AbstractCommand;

/**
 * A command to initialize a composite scoped component on a service node that is included in an activated composite.
 * For example, composite-scoped components included at the domain level.
 *
 * @version $Rev$ $Date$
 */
public class InitializeComponentCommand extends AbstractCommand {
    public static final QName QNAME = new QName(Constants.FABRIC3_NS, "initializeComponentCommand");
    private final URI uri;
    private final URI groupId;

    public InitializeComponentCommand(URI groupId, URI uri, int order) {
        super(order);
        this.groupId = groupId;
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public URI getGroupId() {
        return groupId;
    }
    
    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InitializeComponentCommand) {
            InitializeComponentCommand other = (InitializeComponentCommand) obj;
            return other.uri.equals(uri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
