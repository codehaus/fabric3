/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.tests.function.properties;

import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
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

    Foo getFoo();

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
