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
package org.fabric3.resource.model;

import java.lang.reflect.Member;

import org.fabric3.pojo.scdl.JavaMappedResource;

/**
 *
 * @version $Revision$ $Date$
 */
public class SystemSourcedResource<T> extends JavaMappedResource<T> {
    
    private String mappedName;

    /**
     * @param name
     * @param type
     * @param member
     * @param optional
     * @param mappedName
     */
    public SystemSourcedResource(String name, Class<T> type, Member member, boolean optional, String mappedName) {
        super(name, type, member, optional);
        this.mappedName = mappedName;
    }
    
    /**
     * @return
     */
    public String getMappedName() {
        return this.mappedName;
    }

}
