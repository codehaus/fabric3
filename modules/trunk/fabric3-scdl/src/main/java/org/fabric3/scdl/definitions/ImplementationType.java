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

import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.ModelObject;

/**
 * Represents an implementation type.
 * 
 * @version $Revision$ $Date$
 */
public class ImplementationType extends ModelObject {

    private Set<QName> alwaysProvide;
    private Set<QName> mayProvide;
    private QName name;

    /**
     * @return the alwaysProvide
     */
    public Set<QName> getAlwaysProvide() {
        return alwaysProvide;
    }

    /**
     * @param alwaysProvide
     *            the alwaysProvide to set
     */
    public void setAlwaysProvide(Set<QName> alwaysProvide) {
        this.alwaysProvide = alwaysProvide;
    }

    /**
     * @return the mayProvide
     */
    public Set<QName> getMayProvide() {
        return mayProvide;
    }

    /**
     * @param mayProvide
     *            the mayProvide to set
     */
    public void setMayProvide(Set<QName> mayProvide) {
        this.mayProvide = mayProvide;
    }

    /**
     * @return the name
     */
    public QName getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(QName name) {
        this.name = name;
    }

}
