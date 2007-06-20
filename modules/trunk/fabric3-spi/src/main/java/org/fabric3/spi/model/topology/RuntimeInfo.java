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
package org.fabric3.spi.model.topology;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.model.type.ResourceDescription;

/**
 * Tracks information regarding a runtime service node, including available capabilities and resources
 *
 * @version $Rev$ $Date$
 */
public class RuntimeInfo {

    public static final QName QNAME = new QName(Constants.FABRIC3_NS, "runtimeInfo");

    public enum Status {
        STARTED, STOPPED;
    }

    private String id;
    private List<ResourceDescription<?>> resources;
    private Set<URI> components;
    private Set<QName> features;
    private long uptime;
    private Status status;
    private Object messageDestination;

    public RuntimeInfo() {
        resources = new ArrayList<ResourceDescription<?>>();
        components = new HashSet<URI>();
    }

    public RuntimeInfo(String id) {
        this.id = id;
    }

    /**
     * @return Runtime status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status Runtime status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return Features available on the runtime.
     */
    public Set<QName> getFeatures() {
        return features;
    }

    /**
     * @param features Features available on the runtime.
     */
    public void setFeatures(Set<QName> features) {
        this.features = features;
    }

    /**
     * @return Uptime for the runtime.
     */
    public long getUptime() {
        return uptime;
    }

    /**
     * @param uptime Uptime for the runtime.
     */
    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    /**
     * Returns the runtime id.
     *
     * @return the runtime id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns a list of resource descriptions for available runtime resources such as extensions.
     *
     * @return the list of resource descriptions
     */
    public List<ResourceDescription<?>> getResourceDescriptions() {
        return Collections.unmodifiableList(resources);
    }

    /**
     * Adds a resource description representing an available runtime resources.
     *
     * @param resource the resource description
     */
    public void addResourceDescription(ResourceDescription<?> resource) {
        resources.add(resource);
    }


    /**
     * Returns the list of active components hosted by the runtime.
     *
     * @return the list of active components hosted by the runtime
     */
    public Set<URI> getComponents() {
        return Collections.unmodifiableSet(components);
    }

    /**
     * Adds a a component name to the list of active components hosted by the runtime
     *
     * @param uri the component name
     */
    public void addComponent(URI uri) {
        components.add(uri);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == RuntimeInfo.class && ((RuntimeInfo) obj).equals(id);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @return Message destination used by this runtime.
     */
    public Object getMessageDestination() {
        return messageDestination;
    }

    /**
     * @param messageDestination Message destination used by this runtime.
     */
    public void setMessageDestination(Object messageDestination) {
        this.messageDestination = messageDestination;
    }
}
