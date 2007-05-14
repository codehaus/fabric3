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
package org.fabric3.binding.jms.model;

import java.util.Properties;

import javax.jms.DeliveryMode;

import org.fabric3.spi.model.type.ModelObject;

/**
 * JMS headers.
 * 
 * @version $Revision$ $Date$
 */
public class HeaderDefinition extends ModelObject {
    
    /**
     * Type.
     */
    private String type;
    
    /**
     * Correlation id.
     */
    private String correlationId;
    
    /**
     * Delivery mode.
     */
    private DeliveryMode deliveryMode;
    
    /**
     * Time to live.
     */
    private long timeToLive;
    
    /**
     * Priority.
     */
    private int priority;

    /**
     * Properties.
     */
    private Properties props;

    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * @return the deliveryMode
     */
    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    /**
     * @param deliveryMode the deliveryMode to set
     */
    public void setDeliveryMode(DeliveryMode deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return the props
     */
    public Properties getProps() {
        return props;
    }

    /**
     * @param props the props to set
     */
    public void setProps(Properties props) {
        this.props = props;
    }

    /**
     * @return the timeToLive
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     * @param timeToLive the timeToLive to set
     */
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
}
