/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.common;

/**
 * Represents binding.jms\headers and binding.jms\operationProperties\headers.
 */
public class HeadersDefinition extends PropertyAwareObject {
    private static final long serialVersionUID = 831415808031924363L;
    private String jMSType;
    private String jMSCorrelationId;
    private Integer jMSDeliveryMode;
    private Long jMSTimeToLive;
    private Integer jMSPriority;

    public String getJMSType() {
        return jMSType;
    }

    public void setJMSType(String type) {
        jMSType = type;
    }

    public String getJMSCorrelationId() {
        return jMSCorrelationId;
    }

    public void setJMSCorrelationId(String correlationId) {
        jMSCorrelationId = correlationId;
    }

    public Integer getJMSDeliveryMode() {
        return jMSDeliveryMode;
    }

    public void setJMSDeliveryMode(Integer deliveryMode) {
        jMSDeliveryMode = deliveryMode;
    }

    public Long getJMSTimeToLive() {
        return jMSTimeToLive;
    }

    public void setJMSTimeToLive(Long timeToLive) {
        jMSTimeToLive = timeToLive;
    }

    public Integer getJMSPriority() {
        return jMSPriority;
    }

    public void setJMSPriority(Integer priority) {
        jMSPriority = priority;
    }

    public HeadersDefinition cloneHeadersDefinition() {
        HeadersDefinition clone = new HeadersDefinition();
        clone.setJMSCorrelationId(this.jMSCorrelationId);
        clone.setJMSDeliveryMode(this.jMSDeliveryMode);
        clone.setJMSPriority(this.jMSPriority);
        clone.setJMSTimeToLive(this.jMSTimeToLive);
        clone.setJMSType(this.jMSType);
        clone.setProperties(this.getProperties());
        return clone;
    }

    /**
     * Return a new HeadersDefinition which value is <code>this</code> is shadowed by <code>from</code>
     *
     * @param from the value to shadow from
     * @return the new definition
     */
    public HeadersDefinition shadowHeadersDefinition(HeadersDefinition from) {
        HeadersDefinition result = this.cloneHeadersDefinition();
        if (from.jMSCorrelationId != null) {
            result.setJMSCorrelationId(from.jMSCorrelationId);
        }
        if (from.jMSType != null) {
            result.setJMSType(from.jMSType);
        }
        if (from.jMSDeliveryMode != null) {
            result.setJMSDeliveryMode(from.jMSDeliveryMode);
        }
        if (from.jMSPriority != null) {
            result.setJMSPriority(from.jMSPriority);
        }
        if (from.jMSTimeToLive != null) {
            result.setJMSTimeToLive(from.jMSTimeToLive);
        }
        result.setProperties(from.getProperties());
        return result;
    }
}
