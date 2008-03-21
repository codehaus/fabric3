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
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

/**
 * Test service for returning Properties with different types.
 *
 * @version $Rev$ $Date$
 */
public interface PropertyTypes {
    boolean getBooleanPrimitive();

    byte getBytePrimitive();

    short getShortPrimitive();

    int getIntPrimitive();

    long getLongPrimitive();

    float getFloatPrimitive();

    double getDoublePrimitive();

    Boolean getBooleanValue();

    Byte getByteValue();

    Short getShortValue();

    Integer getIntegerValue();

    Long getLongValue();

    Float getFloatValue();

    Double getDoubleValue();

    Class<?> getClassValue();

    String getString();

    URI getUriValue();

    URL getUrlValue();

    Date getDateValue();

    Calendar getCalendarValue();

    int[] getIntArray();

    Map<String, String> getMapValue();

    Properties getPropertiesValue();
    
    List<String> getListValue();
    
    Map<QName, Class<?>> getMapOfQNameToClassValue();
    
}
