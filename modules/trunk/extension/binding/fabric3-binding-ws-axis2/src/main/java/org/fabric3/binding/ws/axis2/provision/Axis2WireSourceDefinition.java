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
package org.fabric3.binding.ws.axis2.provision;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
public class Axis2WireSourceDefinition extends PhysicalWireSourceDefinition implements Axis2PolicyAware {
    public static final long CACHE_ALL = -1;
    private String serviceInterface;
    private Map<String, Set<AxisPolicy>> policies = new HashMap<String, Set<AxisPolicy>>();
    private URI classloaderURI;
    private boolean cacheLargeAttachements;
    private long cacheThreshold = CACHE_ALL;

    /**
     * @return Service interface for the wire source.
     */
    public String getServiceInterface() {
        return serviceInterface;
    }

    /**
     * @param serviceInterface Service interface for the wire source.
     */
    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * @return Classloader URI.
     */
    public URI getClassloaderURI() {
        return classloaderURI;
    }

    /**
     * @param classloaderURI Classloader URI.
     */
    public void setClassloaderURI(URI classloaderURI) {
        this.classloaderURI = classloaderURI;
    }

    /**
     * @return Policy definitions.
     */
    public Set<AxisPolicy> getPolicies(String operation) {
        return policies.get(operation);
    }

    /**
     * @param policy Policy definitions.
     */
    public void addPolicy(String operation, AxisPolicy policy) {

        if (!this.policies.containsKey(operation)) {
            this.policies.put(operation, new HashSet<AxisPolicy>());
        }
        this.policies.get(operation).add(policy);
    }

    /**
     * @return true if large MTOM attachments should be streamed to disk to avoid buffering in memory.
     */
    public boolean isCacheLargeAttachements() {
        return cacheLargeAttachements;
    }

    /**
     * @param cacheLargeAttachements true if large MTOM attachments should be streamed to disk to avoid buffering in memory.
     */
    public void setCacheLargeAttachements(boolean cacheLargeAttachements) {
        this.cacheLargeAttachements = cacheLargeAttachements;
    }

    /**
     * @return the file size threshold to cache to disk if MTOM file caching is enabled. Returns {@link #CACHE_ALL} if all files should be cached.
     */
    public long getCacheThreshold() {
        return cacheThreshold;
    }

    /**
     * @param threshold the file size threshold to cache to disk if MTOM file caching is enabled.
     */
    public void setCacheThreshold(long threshold) {
        this.cacheThreshold = threshold;
    }
}
