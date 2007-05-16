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

    @Property public String string;
    @Property public Boolean booleanValue;
    @Property public Byte byteValue;
    @Property public Short shortValue;
    @Property public Integer integerValue;
    @Property public Long longValue;
    @Property public Float floatValue;
    @Property public Double doubleValue;
    @Property public Class<?> classValue;

    public boolean getBoolean() {
        return booleanPrimitive;
    }

    public byte getByte() {
        return bytePrimitive;
    }

    public short getShort() {
        return shortPrimitive;
    }

    public int getInt() {
        return intPrimitive;
    }

    public long getLong() {
        return longPrimitive;
    }

    public float getFloat() {
        return floatPrimitive;
    }

    public double getDouble() {
        return doublePrimitive;
    }

    public <T> T getPropertyValue(Class<T> type) {
        if (String.class.equals(type)) {
            return type.cast(string);
        } else if (Boolean.class.equals(type)) {
            return type.cast(booleanValue);
        } else if (Byte.class.equals(type)) {
            return type.cast(byteValue);
        } else if (Short.class.equals(type)) {
            return type.cast(shortValue);
        } else if (Integer.class.equals(type)) {
            return type.cast(integerValue);
        } else if (Long.class.equals(type)) {
            return type.cast(longValue);
        } else if (Float.class.equals(type)) {
            return type.cast(floatValue);
        } else if (Double.class.equals(type)) {
            return type.cast(doubleValue);
        } else if (Class.class.equals(type)) {
            return type.cast(classValue);
        }
        throw new AssertionError();
    }
}
