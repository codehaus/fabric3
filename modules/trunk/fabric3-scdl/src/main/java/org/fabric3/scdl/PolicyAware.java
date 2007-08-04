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

import java.util.Set;

/**
 * Interface that needs to be implemented by any definition object against 
 * which elements and policy sets can be declared.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyAware {
    
    /**
     * @return Returns all the declared intents against the element.
     */
    Set<QName> getIntents();
    
    /**
     * @return Returns all the declared policy sets against the element.
     */
    Set<QName> getPolicySets();
    
    /**
     * @param intentName Add an intent against the element.
     */
    void addIntent(QName intentName);
    
    /**
     * @param policySetName Add a policy set against the element.
     */
    void addPolicySet(QName policySetName);

}
