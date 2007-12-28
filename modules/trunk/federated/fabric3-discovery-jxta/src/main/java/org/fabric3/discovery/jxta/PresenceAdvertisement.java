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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import org.fabric3.scdl.ResourceDescription;
import org.fabric3.spi.model.topology.ClassLoaderResourceDescription;
import org.fabric3.spi.model.topology.RuntimeInfo;

/**
 * @version $Revsion$ $Date$
 */
public class PresenceAdvertisement extends Advertisement {
    /*
     * Register the advertisement type.
     */
    static {
        AdvertisementFactory.registerAdvertisementInstance(getAdvertisementType(), new Instantiator());
    }

    public static final String indexFields[] = new String[]{};

    private URI runtimeId;
    private String peerId;
    private String messageDestination;
    private Set<QName> features = new HashSet<QName>();
    private Set<URI> components = new HashSet<URI>();
    private List<ResourceDescription<?>> resourceDescriptions = new ArrayList<ResourceDescription<?>>();

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
     * @return Runtime info.
     */
    public RuntimeInfo getRuntimeInfo() {
        RuntimeInfo runtimeInfo = new RuntimeInfo(runtimeId);
        runtimeInfo.setFeatures(features);
        runtimeInfo.setMessageDestination(messageDestination);
        for (URI component : components) {
            runtimeInfo.addComponent(component);
        }
        for (ResourceDescription<?> description : resourceDescriptions) {
            runtimeInfo.addResourceDescription(description);
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

        elem = doc.createElement("runtimeId", runtimeId.toString());
        doc.appendChild(elem);

        elem = doc.createElement("messageDestination", messageDestination);
        doc.appendChild(elem);

        // serialize feature qnames
        elem = doc.createElement("features");
        doc.appendChild(elem);
        for (QName feature : features) {
            elem.appendChild(doc.createElement("feature", feature.toString()));
        }

        // serialize component uris
        elem = doc.createElement("components");
        doc.appendChild(elem);
        for (URI component : components) {
            elem.appendChild(doc.createElement("component", component.toString()));
        }

        // serialize resource descriptions
        elem = doc.createElement("resourceDescriptions");
        doc.appendChild(elem);
        for (ResourceDescription<?> description : resourceDescriptions) {
            // TODO this may need to be made extensible
            if (!(ClassLoaderResourceDescription.class.isInstance(description))) {
                String type = description.getClass().getName();
                throw new UnknownResourceDescriptionType("Unknown resource type [" + type + "]", type);
            }
            ClassLoaderResourceDescription clr = ClassLoaderResourceDescription.class.cast(description);
            Element clrElement = doc.createElement("classLoaderResourceDescription");
            elem.appendChild(clrElement);
            Element identifierElement = doc.createElement("identifier", clr.getIdentifier().toString());
            clrElement.appendChild(identifierElement);
            for (URI uri : clr.getParents()) {
                Element parent = doc.createElement("parent", uri.toString());
                clrElement.appendChild(parent);
            }
        }
        return doc;
    }

    public boolean equals(Object target) {
        return target instanceof PresenceAdvertisement && ((PresenceAdvertisement) target).getID().equals(getID());
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException impossible) {
            return null;
        }
    }

    /**
     * @param runtimeInfo Runtime info.
     */
    void setRuntimeInfo(RuntimeInfo runtimeInfo) {
        runtimeId = runtimeInfo.getId();
        if (runtimeInfo.getFeatures() != null) {
            features = runtimeInfo.getFeatures();
        }
        messageDestination = runtimeInfo.getMessageDestination();
        if (runtimeInfo.getComponents() != null) {
            components = runtimeInfo.getComponents();
        }
        if (runtimeInfo.getResourceDescriptions() != null) {
            resourceDescriptions = runtimeInfo.getResourceDescriptions();
        }
    }

    /**
     * Sets the peer id that generated the advertisement.
     *
     * @param peerId Peer id that generated the advertisement
     */
    void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    void setRuntimeId(URI runtimeId) {
        this.runtimeId = runtimeId;
    }

    void setMessageDestination(String messageDestination) {
        this.messageDestination = messageDestination;
    }

    void setFeatures(Set<QName> features) {
        this.features = features;
    }

    void setComponents(Set<URI> components) {
        this.components = components;
    }

    void setResourceDescriptions(List<ResourceDescription<?>> resourceDescriptions) {
        this.resourceDescriptions = resourceDescriptions;
    }

}
