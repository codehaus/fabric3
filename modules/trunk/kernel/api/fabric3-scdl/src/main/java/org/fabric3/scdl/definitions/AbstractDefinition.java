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
package org.fabric3.scdl.definitions;

import javax.xml.namespace.QName;

import org.fabric3.scdl.ModelObject;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractDefinition extends ModelObject {
    
    private final QName name;
    
    /**
     * @param name Name of the definition object.
     */
    public AbstractDefinition(QName name) {
        this.name = name;
    }

    /**
     * @return Qualified name of the definition.
     */
    public final QName getName() {
        return name;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        
        if(other == null) {
            return false;
        }
        
        if(this.getClass() != other.getClass()) {
            return false;
        }
        
        return ((AbstractDefinition) other).name.equals(name);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
