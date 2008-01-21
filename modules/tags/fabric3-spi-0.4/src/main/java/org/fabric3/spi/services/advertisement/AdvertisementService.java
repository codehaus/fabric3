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
package org.fabric3.spi.services.advertisement;

import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Provides the abstraction for locally advertising capaibilities
 * within a runtime. The capabilities are expressed as qualified
 * names.
 *
 * @version $Revsion$ $Date$
 */
public interface AdvertisementService {

    /**
     * Returns the list of features available on the current node.
     * @return List of features.
     */
    Set<QName> getFeatures();

    /**
     * Adds a feature to the current node.
     * @param feature Feature to be added.
     */
    void addFeature(QName feature);

    /**
     * Removes a feature from the current node.
     * @param feature Feature to be removed.
     */
    void removeFeature(QName feature);

    /**
     * Adds an advertismenet listener.
     * @param listener Listener to be added.
     */
    void addListener(AdvertisementListener listener);

    /**
     * Removes an advertismenet listener.
     * @param listener Listener to be removed.
     */
    void removeListener(AdvertisementListener listener);

}
