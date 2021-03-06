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
package org.fabric3.fabric.command;

import java.net.URI;

/**
 *
 * @version $Revision$ $Date$
 */
public class ComponentInitializationUri {
    
    private URI groupId;
    private URI uri;
    
    public URI getGroupId() {
        return groupId;
    }

    public URI getUri() {
        return uri;
    }

    public ComponentInitializationUri(URI groupId, URI uri) {
        super();
        this.groupId = groupId;
        this.uri = uri;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null || obj.getClass() != ComponentInitializationUri.class) {
            return false;
        }
        
        ComponentInitializationUri other = (ComponentInitializationUri) obj;
        return super.equals(uri.equals(other.uri));
        
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

}
