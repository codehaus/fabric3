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
package org.fabric3.scdl;

import javax.xml.namespace.QName;

import org.osoa.sca.Constants;

/**
 * A specialization of an implementation associated with composite components
 *
 * @version $Rev$ $Date$
 */
public class CompositeImplementation extends Implementation<Composite> {
    private static final long serialVersionUID = 2140686609936627287L;
    public static final QName IMPLEMENTATION_COMPOSITE = new QName(Constants.SCA_NS, "implementation.composite");
    private QName name;

    public boolean isComposite() {
        return true;
    }

    public QName getType() {
        return IMPLEMENTATION_COMPOSITE;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

}
