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
package org.fabric3.fabric.services.advertsiement;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.namespace.QName;

import org.fabric3.spi.services.advertisement.AdvertisementListener;
import org.fabric3.spi.services.advertisement.AdvertisementService;

/**
 * Default implementation of the advertisment service.
 *
 * @see org.fabric3.spi.services.advertisement.AdvertisementService
 * @version $Revsion$ $Date$
 */
public class DefaultAdvertismentService implements AdvertisementService {

    // Listeners
    private Set<AdvertisementListener> listeners = new CopyOnWriteArraySet<AdvertisementListener>();

    // Features
    private Set<QName> features = new CopyOnWriteArraySet<QName>();

    /**
     * @see org.fabric3.spi.services.advertisement.AdvertisementService#getFeatures()
     */
    public Set<QName> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    /**
     * @see org.fabric3.spi.services.advertisement.AdvertisementService#addFeature(javax.xml.namespace.QName)
     */
    public void addFeature(QName feature) {
        features.add(feature);
        for(AdvertisementListener listener : listeners) {
            listener.featureAdded(feature);
        }
    }

    /**
     * @see org.fabric3.spi.services.advertisement.AdvertisementService#removeFeature(javax.xml.namespace.QName)
     */
    public void removeFeature(QName feature) {
        features.remove(feature);
        for(AdvertisementListener listener : listeners) {
            listener.featureRemoved(feature);
        }
    }

    /**
     * @see org.fabric3.spi.services.advertisement.AdvertisementService#addListener(org.fabric3.spi.services.advertisement.AdvertisementListener)
     */
    public void addListener(AdvertisementListener listener) {
        listeners.add(listener);
    }

    /**
     * @see org.fabric3.spi.services.advertisement.AdvertisementService#removeListener(org.fabric3.spi.services.advertisement.AdvertisementListener)
     */
    public void removeListener(AdvertisementListener listener) {
        listeners.remove(listener);
    }

}
