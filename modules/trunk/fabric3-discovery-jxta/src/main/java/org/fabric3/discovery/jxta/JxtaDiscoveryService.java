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

import static net.jxta.discovery.DiscoveryService.PEER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jxta.JxtaService;
import org.fabric3.jxta.impl.Fabric3JxtaException;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.advertisement.AdvertisementService;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.work.WorkScheduler;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.thoughtworks.xstream.XStream;

/**
 * JXTA implementation of the discovery service.
 *
 * @version $Revsion$ $Date$
 */
public class JxtaDiscoveryService implements DiscoveryService {

    // Polling interval
    private long interval = 2000L;

    // Expiration threshold
    private long expirationThreshold = 10000L;

    // JXTA Service
    private JxtaService jxtaService;

    // Work scheduler
    private WorkScheduler workScheduler;

    // Host info
    private HostInfo hostInfo;

    // Advertismenet service
    private AdvertisementService advertisementService;

    // Jxta discovery service
    private net.jxta.discovery.DiscoveryService discoveryService;

    // Peer group advertisement
    private PeerAdvertisement peerAdvertisement;

    // Publisher of advertismenets
    private Publisher publisher;

    // Participating runtimes
    private Map<RuntimeInfo, Long> participatingRuntimes = new ConcurrentHashMap<RuntimeInfo, Long>();

    // XStream
    private XStream xstream = new XStream();

    // SAX Parser TODO Not sure whether this is thread safe
    private SAXParser parser;

    /**
     * @see org.fabric3.spi.services.discovery.DiscoveryService#getParticipatingRuntimes()
     */
    public Set<RuntimeInfo> getParticipatingRuntimes() {
        return participatingRuntimes.keySet();
    }

    /**
     * Sets the interval in which discovery messages and advertisements are sent.
     *
     * @param interval Polling interval.
     */
    @Property
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * Sets the expiration threshold after which the runtime expelled.
     *
     * @param expirationThreshold Polling interval.
     */
    @Property
    public void setExpirationThreshold(long expirationThreshold) {
        this.expirationThreshold = expirationThreshold;
    }

    /**
     * Injects the host info.
     *
     * @param Host info to be injected in.
     */
    @Reference
    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * Injects the JXTA service.
     *
     * @param jxtaService JXTA service to be injected in.
     */
    @Reference
    public void setJxtaService(JxtaService jxtaService) {
        this.jxtaService = jxtaService;
    }

    /**
     * Injects the work scheduler.
     *
     * @param workScheduler Work scheduler to be injected in.
     */
    @Reference
    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    /**
     * Starts the service.
     *
     */
    @Init
    public void start() {

        assert workScheduler != null;
        assert jxtaService != null;
        assert hostInfo != null;

        PeerGroup peerGroup = jxtaService.getDomainGroup();

        discoveryService = peerGroup.getDiscoveryService();
        peerAdvertisement = peerGroup.getPeerAdvertisement();

        publisher = new Publisher();
        workScheduler.scheduleWork(publisher);

        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch(ParserConfigurationException ex) {
            throw new Fabric3JxtaException(ex);
        } catch (SAXException ex) {
            throw new Fabric3JxtaException(ex);
        }

    }

    /**
     * Stops the service.
     */
    @Destroy
    public void stop() {
        publisher.live.set(false);
    }

    /*
     * Listener for notifications from other nodes.
     */
    private class Listener implements DiscoveryListener {

        public void discoveryEvent(DiscoveryEvent discoveryEvent) {

            DiscoveryResponseMsg res = discoveryEvent.getResponse();
            Enumeration en = res.getAdvertisements();

            if (en != null ) {

                while (en.hasMoreElements()) {

                    PeerAdvertisement adv = (PeerAdvertisement) en.nextElement();
                    try {

                        String desc = adv.getDesc().toString();
                        WhitespaceStripper stripper = new WhitespaceStripper();
                        parser.parse(new ByteArrayInputStream(desc.getBytes()), stripper);

                        RuntimeInfo runtimeInfo = (RuntimeInfo) xstream.fromXML(stripper.getXml());
                        participatingRuntimes.put(runtimeInfo, System.currentTimeMillis());

                        for(RuntimeInfo info : participatingRuntimes.keySet()) {
                            long lastActive = participatingRuntimes.get(info);
                            if(System.currentTimeMillis() - lastActive > expirationThreshold) {
                                participatingRuntimes.remove(info);
                            }
                        }


                    } catch(SAXException ex) {
                        // TODO Notify the monitor
                    } catch (IOException ex) {
                        // TODO Notify the monitor
                    }

                }

            }

        }

    }

    /*
     * Notifier sending information about the current node.
     *
     */
    private class Publisher implements Runnable {

        private AtomicBoolean live = new AtomicBoolean(true);

        /*
         * Waits for the defined interval and sends advertisements for the
         * current node and discovery requests for the other nodes in the
         * domain.
         */
        public void run() {

            discoveryService.addDiscoveryListener(new Listener());
            while(live.get()) {

                try {

                    Thread.sleep(interval);

                    discoveryService.getRemoteAdvertisements(null, PEER, null, null, 5);

                    RuntimeInfo runtimeInfo = new RuntimeInfo("runtime1");
                    runtimeInfo.setFeatures(advertisementService.getFeatures());

                    String runtimeInfoXml = xstream.toXML(runtimeInfo);
                    peerAdvertisement.setDescription(runtimeInfoXml);

                    discoveryService.publish(peerAdvertisement);
                    discoveryService.remotePublish(peerAdvertisement);

                } catch(InterruptedException ex) {
                    return;
                } catch (IOException e) {
                    // TODO Notify the monitor
                }
            }

        }

    }

    /*
     * Hack for the JXTA RI stone age handling of XML.
     */
    private class WhitespaceStripper extends DefaultHandler {

        StringBuilder builder = new StringBuilder();

        /**
         * Returns the XML stripped of white spaces.
         * @return XStream xml.
         */
        public String getXml() {
            return builder.toString();
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            builder.append(new String(ch, start, length).trim());
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if("Desc".equals(qName)) {
                return;
            }

            builder.append("</" + qName + ">");

        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if("Desc".equals(qName)) {
                return;
            }

            builder.append("<");
            builder.append(qName);
            if(attributes.getLength() > 0) {
                for(int i = 0;i < attributes.getLength();i++) {
                    builder.append(" ");
                    builder.append(attributes.getQName(i));
                    builder.append("=");
                    builder.append("'");
                    builder.append(attributes.getValue(i));
                    builder.append("'");
                }
            }
            builder.append(">");
        }

    }

}
