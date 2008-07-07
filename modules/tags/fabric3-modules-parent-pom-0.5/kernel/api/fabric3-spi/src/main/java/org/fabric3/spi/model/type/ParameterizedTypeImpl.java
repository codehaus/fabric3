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
