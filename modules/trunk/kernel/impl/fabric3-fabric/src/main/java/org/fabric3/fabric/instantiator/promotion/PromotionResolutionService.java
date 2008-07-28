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
package org.fabric3.fabric.instantiator.promotion;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Resolves promoted services and references by setting the resolved promotion URI of the logical component service or reference that is being
 * promoted.
 *
 * @version $Revision$ $Date$
 */
public interface PromotionResolutionService {

    /**
     * Handles promotion on the specified logical service.
     * <p/>
     * Promoted URIs are of the general form <code>componentId#serviceName</code>, where the service name is optional. If the  promoted URI doesn't
     * contain a fragment for the service name, the promoted component is expected to have exactly one service. If the service fragment is present the
     * promoted component is required to have a service by the name. If the service fragment was not specified, the promoted URI is set to the URI of
     * the promoted service.
     *
     * @param logicalService Logical service whose promotion is handled.
     * @param change         the logical change associated with the deployment operation resolution is being performed for. Recoverable errors and
     *                       warnings should be reported here.
     */
    void resolve(LogicalService logicalService, LogicalChange change);

    /**
     * Handles all promotions on the specified logical reference.
     * <p/>
     * Promoted URIs are of the general form <code>componentId#referenceName</code>, where the reference name is optional. If the  promoted URI
     * doesn't contain a fragment for the reference name, the promoted component is expected to have exactly one reference. If the reference fragment
     * is present the promoted component is required to have a reference by the name. If the reference fragment was not specified, the promoted URI is
     * set to the URI of the promoted reference.
     *
     * @param logicalReference Logical reference whose promotion is handled.
     * @param change           the logical change associated with the deployment operation resolution is being performed for. Recoverable errors and
     *                         warnings should be reported here.
     */
    void resolve(LogicalReference logicalReference, LogicalChange change);

}
