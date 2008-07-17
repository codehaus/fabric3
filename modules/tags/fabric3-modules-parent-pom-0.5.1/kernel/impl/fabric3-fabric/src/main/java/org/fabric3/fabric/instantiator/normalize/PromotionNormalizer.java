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
package org.fabric3.fabric.instantiator.normalize;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Merges binding and other metadata on promoted services and references down the to leaf component they are initially defined on.
 *
 * @version $Rev$ $Date$
 */
public interface PromotionNormalizer {

    /**
     * Performs the normalization operation on services and references defined by the given leaf component. The hierarchy of containing components
     * will be walked to determine the set of promoted services and references.
     *
     * @param component the leaf component
     */
    void normalize(LogicalComponent<?> component);

}
