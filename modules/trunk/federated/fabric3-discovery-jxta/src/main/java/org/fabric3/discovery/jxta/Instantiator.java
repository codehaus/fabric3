/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.discovery.jxta;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

        elem = (Element) element.getChildren("transportInfo").nextElement();
        Map<QName, String> info = deserializeTransportInfo(elem);
        adv.setTransportInfo(info);

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

    private Map<QName, String> deserializeTransportInfo(Element elem) {
        Map<QName, String> info = new HashMap<QName, String>();
        Enumeration children = elem.getChildren("metaData");
        while (children.hasMoreElements()) {
            // read the metadata for each transport keyed by a QName representing the transport
            elem = (Element) children.nextElement();
            Enumeration infoChildren = elem.getChildren();
            QName key = QName.valueOf((String) ((Element) infoChildren.nextElement()).getValue());
            String value = (String) ((Element) infoChildren.nextElement()).getValue();
            info.put(key, value);
        }
        return info;
    }

}
