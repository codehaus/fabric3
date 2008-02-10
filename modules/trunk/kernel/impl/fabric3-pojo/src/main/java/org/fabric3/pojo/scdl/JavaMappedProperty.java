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
package org.fabric3.pojo.scdl;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Property;
import org.fabric3.scdl.MemberSite;

/**
 * A Property definition that is mapped to a specific location in the implementation class. This location will typically
 * be used to inject property values.
 *
 * @version $Rev$ $Date$
 */
public class JavaMappedProperty<T> extends Property<T> {
    private MemberSite memberSite;

    public JavaMappedProperty() {
    }

    public JavaMappedProperty(String name, QName xmlType, Class<T> javaType, MemberSite memberSite) {
        super(name, xmlType, javaType);
        this.memberSite = memberSite;
    }

    /**
     * Returns the MemberSite that this property is mapped to.
     *
     * @return the MemberSite that this property is mapped to
     */
    public MemberSite getMemberSite() {
        return memberSite;
    }

    /**
     * Sets the MemberSite that this property is mapped to
     *
     * @param memberSite the MemberSite that this property is mapped to
     */
    public void setMemberSite(MemberSite memberSite) {
        this.memberSite = memberSite;
    }
}
