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
package org.fabric3.discovery.jxta;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;

import org.fabric3.jxta.impl.Fabric3JxtaException;
import org.fabric3.spi.model.topology.RuntimeInfo;

/**
 *
 * @version $Revsion$ $Date$
 *
 */
public class PresenceAdvertisement extends Advertisement {

    /*
     * Register the advertisement type.
     */
    static {
        AdvertisementFactory.registerAdvertisementInstance(getAdvertisementType(), new PresenceAdvertisement.Instantiator());
    }

    /*
     * Indexed fields.
     */
    public static final String indexFields[] = new String[] {};

    /*
     * Runtime id.
     */
    private URI runtimeId;

    /*
     * Peer id.
     */
    private String peerId;

    /*
     * Message destination.
     */
    private String messageDestination;

    /*
     * Features.
     */
    private Set<QName> features = new HashSet<QName>();

    /*
     * Component URIs.
     *
     */
    private Set<URI> components = new HashSet<URI>();

    /*
     * Instantiator.
     */
    public static final class Instantiator implements AdvertisementFactory.Instantiator {

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
                adv.runtimeId = URI.create(elem.getValue().toString());
            }

            elem = (Element) element.getChildren("peerId").nextElement();
            if (elem != null && elem.getValue() != null) {
                adv.setPeerId(elem.getValue().toString());
            }

            elem = (Element) element.getChildren("messageDestination").nextElement();
            if (elem != null && elem.getValue() != null) {
                adv.messageDestination = elem.getValue().toString();
            }

            elem = (Element) element.getChildren("features").nextElement();
            adv.features = new HashSet<QName>();
            Enumeration children = elem.getChildren("feature");
            while(children.hasMoreElements()) {
                elem = (Element) children.nextElement();
                if (elem != null && elem.getValue() != null) {
                    adv.features.add(QName.valueOf(elem.getValue().toString()));
                }
            }

            elem = (Element) element.getChildren("components").nextElement();
            adv.components = new HashSet<URI>();
            children = elem.getChildren("component");
            while(children.hasMoreElements()) {
                elem = (Element) children.nextElement();
                if (elem != null && elem.getValue() != null) {
                    try {
                        adv.components.add(new URI(elem.getValue().toString()));
                    } catch (URISyntaxException ex) {
                        throw new Fabric3JxtaException(ex);
                    }
                }
            }

            return adv;

        }

    }

    /**
     * @param runtimeInfo Runtime info.
     */
    public void setRuntimeInfo(RuntimeInfo runtimeInfo) {
        runtimeId = runtimeInfo.getId();
        if(runtimeInfo.getFeatures() != null) {
            features = runtimeInfo.getFeatures();
        }
        messageDestination = runtimeInfo.getMessageDestination();
        if(runtimeInfo.getComponents() != null) {
            components = runtimeInfo.getComponents();
        }
    }

    /**
     * @return Runtime info.
     */
    public RuntimeInfo getRuntimeInfo() {
        RuntimeInfo runtimeInfo = new RuntimeInfo(runtimeId);
        runtimeInfo.setFeatures(features);
        runtimeInfo.setMessageDestination(messageDestination);
        for(URI component : components) {
            runtimeInfo.addComponent(component);
        }
        return runtimeInfo;
    }

    /**
     * @return Advertisement type.
     */
    public static String getAdvertisementType() {
        return "jxta:f3-presence";
    }

    /**
     * @return Base advertisement type.
     */
    public final String getBaseAdvType() {
        return getAdvertisementType();
    }

    public Document getDocument(MimeMediaType mimeMediaType) {

        StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(mimeMediaType, getAdvertisementType());

        Element elem = doc.createElement("peerId", peerId);
        doc.appendChild(elem);

        elem = doc.createElement("runtimeId", runtimeId);
        doc.appendChild(elem);

        elem = doc.createElement("messageDestination", messageDestination);
        doc.appendChild(elem);

        elem = doc.createElement("features");
        doc.appendChild(elem);
        for(QName feature : features) {
            elem.appendChild(doc.createElement("feature", feature.toString()));
        }

        elem = doc.createElement("components");
        doc.appendChild(elem);
        for(URI component : components) {
            elem.appendChild(doc.createElement("component", component.toString()));
        }

        return doc;
    }

    @SuppressWarnings("deprecation")
    public ID getID() {
        try {
            return IDFactory.fromURL(IDFactory.jxtaURL(peerId));
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getIndexFields() {
        return indexFields;
    }

    /**
     * @return Peer id that generated the advertisement.
     */
    public String getPeerId() {
        return peerId;
    }

    /**
     * @param peerId Peer id that generated the advertisement.
     */
    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public boolean equals(Object target) {
        if (target instanceof PresenceAdvertisement) {
            return ((PresenceAdvertisement) target).getID().equals(getID());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException impossible) {
            return null;
        }
    }

}
