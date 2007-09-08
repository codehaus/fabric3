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
package org.fabric3.spi.model.instance;

/**
 * Identifies the source of a value supplied from SCA to an implementation. This is essentially something that can be
 * configured through SCDL. Currently supported types are Services (primarily to support callbacks), References and
 * Properties.
 *
 * @version $Revision$ $Date$
 */
public class ValueSource {
    /**
     * Enumeration of the type of ValueSource supported.
     */
    public static enum ValueSourceType {
        SERVICE,
        REFERENCE,
        PROPERTY
    }

    private ValueSourceType valueType;

    private String name;

    /**
     * Constructor used for desearialization.
     */
    public ValueSource() {
    }

    /**
     * Constructor specifying type of value and logical name.
     *
     * @param valueType the type of value
     * @param name      the logical name
     */
    public ValueSource(ValueSourceType valueType, String name) {
        this.valueType = valueType;
        this.name = name;
    }

    /**
     * Returns the type (service, reference, property).
     *
     * @return the type of value this source represents
     */
    public ValueSourceType getValueType() {
        return valueType;
    }

    /**
     * Sets the type (callback, reference, property).
     *
     * @param valueType the type of value this source represents
     */
    public void setValueType(ValueSourceType valueType) {
        this.valueType = valueType;
    }

    /**
     * Returns the name.
     *
     * @return the name of this value
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this value.
     *
     * @param name the name of this value
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int result = 31 + ((name == null) ? 0 : name.hashCode());
        result = 31 * result + ((valueType == null) ? 0 : valueType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValueSource other = (ValueSource) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (valueType == null) {
            if (other.valueType != null) {
                return false;
            }
        } else if (valueType != other.valueType) {
            return false;
        }
        return true;
    }

}
