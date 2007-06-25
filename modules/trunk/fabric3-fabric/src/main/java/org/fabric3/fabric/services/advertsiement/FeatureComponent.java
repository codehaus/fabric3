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

import javax.xml.namespace.QName;

import org.fabric3.spi.services.advertisement.AdvertisementService;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revsion$ $Date$
 */
@EagerInit
public class FeatureComponent {

    // Feature // TODO support list of features
    private QName feature;

    // Advertisement service
    private AdvertisementService advertisementService;

    /**
     * @param advertisementService Advertisement se
     */
    @Reference
    public void setAdvertisementService(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    /**
     * @param feature Feature injected as a property.
     */
    @Property
    public void setFeature(QName feature) {
        this.feature = feature;
    }

    /**
     * Registers the feature with the advertisement service.
     */
    @Init
    public void start() {
        advertisementService.addFeature(feature);
    }

}
