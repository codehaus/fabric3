package org.fabric3.fabric.command;

import java.net.URI;

import org.fabric3.spi.command.AbstractCommand;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

public class UnprovisionClassloaderCommand extends AbstractCommand {

    private final URI uri;
    public UnprovisionClassloaderCommand(int order, URI uri) {
        super(order);
        this.uri = uri;
        assert uri != null;
    }

    public URI getUri() {
        return uri;
    }

    public int hashCode() {
        return uri.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        try {
            UnprovisionClassloaderCommand other = (UnprovisionClassloaderCommand) obj;
            return uri.equals(other.uri);
        } catch (ClassCastException cce) {
            return false;
        }
    }

}

