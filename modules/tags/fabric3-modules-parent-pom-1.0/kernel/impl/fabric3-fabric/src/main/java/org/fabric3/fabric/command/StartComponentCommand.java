/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.command;

import java.net.URI;

import org.fabric3.spi.command.AbstractCommand;

/**
 * A command to start a component on a runtime.
 *
 * @version $Revision$ $Date$
 */
public class StartComponentCommand extends AbstractCommand {
    private static final long serialVersionUID = -6106317903755129824L;

    private final URI uri;

    public StartComponentCommand(int order, URI uri) {
        super(order);
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartComponentCommand that = (StartComponentCommand) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (uri != null ? uri.hashCode() : 0);
    }
}
