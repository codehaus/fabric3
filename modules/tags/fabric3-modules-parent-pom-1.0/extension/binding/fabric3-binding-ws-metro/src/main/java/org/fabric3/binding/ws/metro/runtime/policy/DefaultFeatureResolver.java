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
package org.fabric3.binding.ws.metro.runtime.policy;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.fabric3.model.type.definitions.PolicySet;

import com.sun.xml.ws.developer.BindingTypeFeature;
import com.sun.xml.ws.developer.JAXWSProperties;

public class DefaultFeatureResolver implements FeatureResolver {
    
    /**
     * Translates the requested intents and policy sets to web service features.
     * 
     * @param requestedIntents Requested intents.
     * @param requestedPolicySets Requested policy sets.
     * @return Rsolved feature sets.
     */
    public WebServiceFeature[] getFeatures(List<QName> requestedIntents, List<PolicySet> requestedPolicySets) {
        
        List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        if (requestedIntents.contains(MayProvidedIntents.MESSAGE_OPTIMISATION)) {
            features.add(new MTOMFeature());
        }
        if (requestedIntents.contains(MayProvidedIntents.PROTOCOL_REST)) {
            features.add(new BindingTypeFeature(JAXWSProperties.REST_BINDING));
        }
        
        WebServiceFeature[] webServiceFeatures = new WebServiceFeature[features.size()];
        webServiceFeatures = features.toArray(webServiceFeatures);
        
        return webServiceFeatures;
        
    }

}
