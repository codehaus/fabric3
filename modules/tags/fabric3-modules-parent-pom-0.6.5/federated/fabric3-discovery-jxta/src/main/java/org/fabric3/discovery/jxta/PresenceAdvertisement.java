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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

import org.fabric3.spi.model.topology.RuntimeInfo;

/**
 * Encapsulates metadata for a runtime sent to the controller.
 *
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
    private Map<QName, String> transportInfo = new HashMap<QName, String>();

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
     * Returs the peer id that generated the advertisement.
     *
     * @return Peer id that generated the advertisement.
     */
    public String getPeerId() {
        return peerId;
    }

    /**
     * Returns the RuntimeInfo for the runtime.
     *
     * @return Runtime info.
     */
    public RuntimeInfo getRuntimeInfo() {
        RuntimeInfo runtimeInfo = new RuntimeInfo(runtimeId);
        runtimeInfo.setFeatures(features);
        runtimeInfo.setMessageDestination(messageDestination);
        for (URI component : components) {
            runtimeInfo.addComponent(component);
        }
        runtimeInfo.setTransportInfo(transportInfo);
        return runtimeInfo;
    }

    /**
     * Sets the RuntimeInfo for the runtime.
     *
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
        if (runtimeInfo.getTransportInfo() != null) {
            transportInfo = runtimeInfo.getTransportInfo();
        }
    }


    /**
     * Returns the advertisement type.
     *
     * @return Advertisement type.
     */
    public static String getAdvertisementType() {
        return "jxta:f3-presence";
    }

    /**
     * Returns the base advertisement type.
     *
     * @return Base advertisement type.
     */
    public final String getBaseAdvType() {
        return getAdvertisementType();
    }

    /**
     * Sets the peer id that generated the advertisement.
     *
     * @param peerId Peer id that generated the advertisement
     */
    void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    /**
     * Sets the runtime id for the runtime generating the advertisement.
     *
     * @param runtimeId the runtime id
     */
    void setRuntimeId(URI runtimeId) {
        this.runtimeId = runtimeId;
    }

    /**
     * Sets the opaque message destination metadata for how the runtime may be contacted.
     *
     * @param messageDestination the opaque message destination information
     */
    void setMessageDestination(String messageDestination) {
        this.messageDestination = messageDestination;
    }

    /**
     * Sets the features available on the runtime.
     *
     * @param features the features available on the runtime
     */
    void setFeatures(Set<QName> features) {
        this.features = features;
    }

    /**
     * Sets the active components hosted by the runtime.
     *
     * @param components the components hosted by the runtime
     */
    void setComponents(Set<URI> components) {
        this.components = components;
    }

    /**
     * Sets the opaque metadata for binding transports supported by the runtime keyed by transport type
     *
     * @param info the transport metadata
     */
    void setTransportInfo(Map<QName, String> info) {
        transportInfo = info;
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

        // serialize runtime transport information keyed by transport QName
        elem = doc.createElement("transportInfo");
        doc.appendChild(elem);
        for (Map.Entry<QName, String> entry : transportInfo.entrySet()) {
            Element infoElement = doc.createElement("metaData");
            elem.appendChild(infoElement);
            infoElement.appendChild(doc.createElement("key", entry.getKey().toString()));
            infoElement.appendChild(doc.createElement("value", entry.getValue()));
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


}
