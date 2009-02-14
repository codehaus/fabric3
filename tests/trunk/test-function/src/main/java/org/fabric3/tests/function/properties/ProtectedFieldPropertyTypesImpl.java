/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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

import org.oasisopen.sca.annotation.Property;

/**
 * @version $Rev$ $Date$
 */
public class ProtectedFieldPropertyTypesImpl implements PropertyTypes {

    @Property
    protected Foo foo;

    @Property
    protected boolean booleanPrimitive;
    @Property
    protected byte bytePrimitive;
    @Property
    protected short shortPrimitive;
    @Property
    protected int intPrimitive;
    @Property
    protected long longPrimitive;
    @Property
    protected float floatPrimitive;
    @Property
    protected double doublePrimitive;

    @Property
    protected Boolean booleanValue;
    @Property
    protected Byte byteValue;
    @Property
    protected Short shortValue;
    @Property
    protected Integer integerValue;
    @Property
    protected Long longValue;
    @Property
    protected Float floatValue;
    @Property
    protected Double doubleValue;
    @Property
    protected Class<?> classValue;

    @Property
    protected String string;
    @Property
    protected URI uriValue;
    @Property
    protected URL urlValue;
    @Property
    protected Date dateValue;
    @Property
    protected Calendar calendarValue;

    @Property (required = false)
    protected int[] intArray;
    @Property
    protected Map<String, String> mapValue;
    @Property
    protected Properties propertiesValue;
    @Property
    protected List<String> listValue;
    @Property
    protected Map<QName, Class<?>> mapOfQNameToClassValue;

    public Foo getFoo() {
        return foo;
    }

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

    public Date getDateValue() {
        return dateValue;
    }

    public Calendar getCalendarValue() {
        return calendarValue;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public Map<String, String> getMapValue() {
        return mapValue;
    }

    public Properties getPropertiesValue() {
        return propertiesValue;
    }

    public List<String> getListValue() {
        return listValue;
    }

    public Map<QName, Class<?>> getMapOfQNameToClassValue() {
        return mapOfQNameToClassValue;
    }
}