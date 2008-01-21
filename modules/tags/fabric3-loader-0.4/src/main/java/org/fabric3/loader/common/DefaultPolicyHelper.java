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
package org.fabric3.loader.common;

import javax.xml.stream.XMLStreamReader;

import org.fabric3.scdl.PolicyAware;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Default implementation of the policy helper.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultPolicyHelper implements PolicyHelper {

    public void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader) throws LoaderException {
        
        policyAware.setIntents(StaxUtil.parseListOfQNames(reader, "requires"));
        policyAware.setPolicySets(StaxUtil.parseListOfQNames(reader, "policySets"));

    }

}
