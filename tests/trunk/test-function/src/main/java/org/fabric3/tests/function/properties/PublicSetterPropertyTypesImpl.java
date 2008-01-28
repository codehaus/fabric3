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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.osoa.sca.annotations.Property;

/**
 * @version $Rev$ $Date$
 */
public class PublicSetterPropertyTypesImpl implements PropertyTypes {
    private boolean booleanPrimitive;
    private byte bytePrimitive;
    private short shortPrimitive;
    private int intPrimitive;
    private long longPrimitive;
    private float floatPrimitive;
    private double doublePrimitive;

    private Boolean booleanValue;
    private Byte byteValue;
    private Short shortValue;
    private Integer integerValue;
    private Long longValue;
    private Float floatValue;
    private Double doubleValue;
    private Class<?> classValue;

    private String string;
    private URI uriValue;
    private URL urlValue;
    private Date dateValue;
    private Calendar calendarValue;

    private int[] intArray;
    private Map<String, String> map;

    public boolean getBooleanPrimitive() {
        return booleanPrimitive;
    }

    @Property
    public void setBooleanPrimitive(boolean booleanPrimitive) {
        this.booleanPrimitive = booleanPrimitive;
    }

    public byte getBytePrimitive() {
        return bytePrimitive;
    }

    @Property
    public void setBytePrimitive(byte bytePrimitive) {
        this.bytePrimitive = bytePrimitive;
    }

    public short getShortPrimitive() {
        return shortPrimitive;
    }

    @Property
    public void setShortPrimitive(short shortPrimitive) {
        this.shortPrimitive = shortPrimitive;
    }

    public int getIntPrimitive() {
        return intPrimitive;
    }

    @Property
    public void setIntPrimitive(int intPrimitive) {
        this.intPrimitive = intPrimitive;
    }

    public long getLongPrimitive() {
        return longPrimitive;
    }

    @Property
    public void setLongPrimitive(long longPrimitive) {
        this.longPrimitive = longPrimitive;
    }

    public float getFloatPrimitive() {
        return floatPrimitive;
    }

    @Property
    public void setFloatPrimitive(float floatPrimitive) {
        this.floatPrimitive = floatPrimitive;
    }

    public double getDoublePrimitive() {
        return doublePrimitive;
    }

    @Property
    public void setDoublePrimitive(double doublePrimitive) {
        this.doublePrimitive = doublePrimitive;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    @Property
    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Byte getByteValue() {
        return byteValue;
    }

    @Property
    public void setByteValue(Byte byteValue) {
        this.byteValue = byteValue;
    }

    public Short getShortValue() {
        return shortValue;
    }

    @Property
    public void setShortValue(Short shortValue) {
        this.shortValue = shortValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    @Property
    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    @Property
    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    @Property
    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    @Property
    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Class<?> getClassValue() {
        return classValue;
    }

    @Property
    public void setClassValue(Class<?> classValue) {
        this.classValue = classValue;
    }

    public String getString() {
        return string;
    }

    @Property
    public void setString(String string) {
        this.string = string;
    }

    public URI getUriValue() {
        return uriValue;
    }

    @Property
    public void setUriValue(URI uriValue) {
        this.uriValue = uriValue;
    }

    public URL getUrlValue() {
        return urlValue;
    }

    @Property
    public void setUrlValue(URL urlValue) {
        this.urlValue = urlValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    @Property
    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Calendar getCalendarValue() {
        return calendarValue;
    }

    @Property
    public void setCalendarValue(Calendar calendarValue) {
        this.calendarValue = calendarValue;
    }

    public int[] getIntArray() {
        return intArray;
    }

    @Property
    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public Map<String, String> getMapValue() {
        return map;
    }

    @Property
    public void setMapValue(Map<String, String> map) {
        this.map = map;
    }
}