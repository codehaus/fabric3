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
package org.fabric3.binding.jms.common;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class JmsURIMetadata {
    public final static String CONNECTIONFACORYNAME = "connectionFactoryName";
    public final static String DESTINATIONTYPE = "destinationType";
    public final static String DELIVERYMODE = "deliveryMode";
    public final static String TIMETOLIVE = "timeToLive";
    public final static String PRIORITY = "priority";
    public final static String RESPONSEDESTINAT = "responseDestination";
    /**
     * string representative for destination
     */
    private String destination;
    /**
     * property map
     */
    private Map<String, String> properties;

    public String getDestination() {
        return destination;
    }

    private JmsURIMetadata(String destination) {
        this.destination = destination;
        properties = new HashMap<String, String>();
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Get a JmsURIMetadata from an input string.
     *
     * @param uri string for /binding.jms/@uri
     * @return a JmsURIMetadata
     * @throws URISyntaxException Thrown when <code>uri</code> is not a valid format required by /binding.jms/@uri.
     */
    public static JmsURIMetadata parseURI(String uri) throws URISyntaxException {
        //TODO have a better validation
        boolean matches = Pattern.matches(
                "jms:(.*?)[\\?(.*?)=(.*?)((&(.*?)=(.*?))*)]?", uri);
        if (!matches) {
            throw new URISyntaxException(uri, "Not a valid URI format for binding.jms");
        }
        return doParse(uri);
    }

    private static JmsURIMetadata doParse(String uri) {
        StringTokenizer token = new StringTokenizer(uri, ":?=&");
        String current;
        String propertyName = null;
        int pos = 0;
        JmsURIMetadata result = null;
        while (token.hasMoreTokens()) {
            current = token.nextToken();
            if (1 == pos) {
                result = new JmsURIMetadata(current);
            } else if (pos % 2 == 0) {
                propertyName = current;
            } else if (0 != pos) {// ignore beginning 'jms'
                assert propertyName != null;
                result.properties.put(propertyName.trim(), current.trim());
            }
            pos++;
        }
        return result;
    }

}
