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
package org.fabric3.spi.model.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @version $Revision$ $Date$
 */
public final class ParameterizedTypeImpl implements ParameterizedType {
    
    private final Type[] actualTypeArguments;
    private final Type rawType;

    public ParameterizedTypeImpl(Type[] actualTypeArguments, Type rawType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
    }

    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public Type getOwnerType() {
        return null;
    }

    public Type getRawType() {
        return rawType;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (!(obj instanceof ParameterizedType)) {
            return false;
        }
        
        ParameterizedType other = (ParameterizedType) obj;
        Type[] otherTypeArguments = other.getActualTypeArguments();
        
        boolean equals = rawType.equals(other.getRawType());
        equals = actualTypeArguments.length == otherTypeArguments.length;
        
        for (int i = 0;i < actualTypeArguments.length;i++) {
            equals = actualTypeArguments[i].equals(otherTypeArguments[i]);
        }
        
        return equals;
        
    }

    @Override
    public int hashCode() {
        
        int hash = 7;
        hash = 31 * hash + rawType.hashCode();
        for (Type actualTypeArgument : actualTypeArguments) {
            hash = 31 * hash + actualTypeArgument.hashCode();
        }
        
        return hash;
    }

}
