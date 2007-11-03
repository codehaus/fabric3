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
package org.fabric3.tests.function.properties;

import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.Property;

/**
 * @version $Rev$ $Date$
 */
public class PropertyTypesImpl implements PropertyTypes {
    @Property public boolean booleanPrimitive;
    @Property public byte bytePrimitive;
    @Property public short shortPrimitive;
    @Property public int intPrimitive;
    @Property public long longPrimitive;
    @Property public float floatPrimitive;
    @Property public double doublePrimitive;

    @Property public Boolean booleanValue;
    @Property public Byte byteValue;
    @Property public Short shortValue;
    @Property public Integer integerValue;
    @Property public Long longValue;
    @Property public Float floatValue;
    @Property public Double doubleValue;
    @Property public Class<?> classValue;

    @Property public String string;
    @Property public URI uriValue;
    @Property public URL urlValue;

    public boolean getBooleanPrimitive() {
        return booleanPrimitive;
    }

    public byte getBytePrimitive() {
        return bytePrimitive;
    }

    public short getShortPrimitive() {
        return shortPrimitive;
    }

    public int getIntPrimitive() {
        return intPrimitive;
    }

    public long getLongPrimitive() {
        return longPrimitive;
    }

    public float getFloatPrimitive() {
        return floatPrimitive;
    }

    public double getDoublePrimitive() {
        return doublePrimitive;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public Byte getByteValue() {
        return byteValue;
    }

    public Short getShortValue() {
        return shortValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public Class<?> getClassValue() {
        return classValue;
    }

    public String getString() {
        return string;
    }

    public URI getUriValue() {
        return uriValue;
    }

    public URL getUrlValue() {
        return urlValue;
    }
}
