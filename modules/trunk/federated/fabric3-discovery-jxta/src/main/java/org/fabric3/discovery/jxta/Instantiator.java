/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.discovery.jxta;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;

import org.fabric3.jxta.impl.Fabric3JxtaException;

/**
 * Instantiates a PresenceAdvertisement from a serialized JXTA message.
 *
 * @version $Rev$ $Date$
 */
public class Instantiator implements AdvertisementFactory.Instantiator {


    public String getAdvertisementType() {
        return PresenceAdvertisement.getAdvertisementType();
    }

    public Advertisement newInstance() {
        return new PresenceAdvertisement();
    }

    public Advertisement newInstance(Element element) {

        PresenceAdvertisement adv = new PresenceAdvertisement();

        Element elem = (Element) element.getChildren("runtimeId").nextElement();
        if (elem != null && elem.getValue() != null) {
            adv.setRuntimeId(URI.create(elem.getValue().toString()));
        }

        elem = (Element) element.getChildren("peerId").nextElement();
        if (elem != null && elem.getValue() != null) {
            adv.setPeerId(elem.getValue().toString());
        }

        elem = (Element) element.getChildren("messageDestination").nextElement();
        if (elem != null && elem.getValue() != null) {
            adv.setMessageDestination(elem.getValue().toString());
        }

        elem = (Element) element.getChildren("features").nextElement();
        Set<QName> features = deserializeFeatures(elem);
        adv.setFeatures(features);

        elem = (Element) element.getChildren("components").nextElement();
        Set<URI> compenents = deserializeComponents(elem);
        adv.setComponents(compenents);

        return adv;

    }

    private Set<URI> deserializeComponents(Element elem) {
        Set<URI> components = new HashSet<URI>();
        Enumeration children = elem.getChildren("component");
        while (children.hasMoreElements()) {
            elem = (Element) children.nextElement();
            if (elem != null && elem.getValue() != null) {
                try {
                    components.add(new URI(elem.getValue().toString()));
                } catch (URISyntaxException ex) {
                    throw new Fabric3JxtaException(ex);
                }
            }
        }
        return components;
    }

    private Set<QName> deserializeFeatures(Element elem) {
        Set<QName> features = new HashSet<QName>();
        Enumeration children = elem.getChildren("feature");
        while (children.hasMoreElements()) {
            elem = (Element) children.nextElement();
            if (elem != null && elem.getValue() != null) {
                features.add(QName.valueOf(elem.getValue().toString()));
            }
        }
        return features;
    }

}
