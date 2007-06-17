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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.id.CBID.ModuleClassID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.PeerAdvertisement;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jxta.impl.JxtaServiceImpl;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.XStream;

/**
 * @version $Revsion$ $Date$
 */
public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        final URI domain = new URI("domain1");
        final String runtimeId = "runtime1";

        HostInfo hostInfo = new HostInfo() {

            public URL getBaseURL() {
                return null;
            }

            public URI getDomain() {
                return domain;
            }

            public String getRuntimeId() {
                return runtimeId;
            }

            public boolean isOnline() {
                return false;
            }

        };

        NetworkConfigurator networkConfigurator = new NetworkConfigurator();
        networkConfigurator.setPrincipal("test-user");
        networkConfigurator.setPassword("test-password");

        JxtaServiceImpl jxtaService = new JxtaServiceImpl();
        jxtaService.setHostInfo(hostInfo);
        jxtaService.setNetworkConfigurator(networkConfigurator);
        jxtaService.start();

        PeerGroup domainGroup = jxtaService.getDomainGroup();

        RuntimeInfo runtimeInfo = new RuntimeInfo("runtime1");
        runtimeInfo.setFeatures(Collections.singleton(new QName("http://www.fabric3.org", "transactional")));

        XStream xstream = new XStream();
        String xml = xstream.toXML(runtimeInfo);

        PeerAdvertisement peerAdvertisement = domainGroup.getPeerAdvertisement();

        peerAdvertisement.setDescription(xml);

        // peerAdvertisement.getDocument(new MimeMediaType("text/xml")).sendToStream(System.out);

        // This is brute force XML as JXTA was hacked in the ice age :(

        String startElement = "<org.fabric3.spi.model.topology.RuntimeInfo>";
        String endElement = "</org.fabric3.spi.model.topology.RuntimeInfo>";

        xml = peerAdvertisement.getDesc().toString();

        System.err.println(xml.indexOf(startElement));
        System.err.println(xml.lastIndexOf(startElement));

        // xml = xml.substring(xml.indexOf(startElement), xml.lastIndexOf(endElement) + endElement.length());

        System.err.println(xml);

        final StringBuilder builder = new StringBuilder();

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new ByteArrayInputStream(xml.getBytes()), new DefaultHandler() {

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

        });

        System.err.println(builder.toString());

        runtimeInfo = (RuntimeInfo) xstream.fromXML(new ByteArrayInputStream(builder.toString().getBytes()));
        System.err.println(runtimeInfo.getFeatures());

        System.exit(0);

    }

}
