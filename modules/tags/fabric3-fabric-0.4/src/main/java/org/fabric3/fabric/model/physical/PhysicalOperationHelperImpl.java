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
package org.fabric3.fabric.model.physical;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * @version $Revision$ $Date$
 */
public class PhysicalOperationHelperImpl implements PhysicalOperationHelper {

    /**
     * @see org.fabric3.fabric.model.physical.PhysicalOperationHelper#mapOperation(org.fabric3.scdl.Operation)
     */
    @SuppressWarnings({"unchecked"})
    public PhysicalOperationDefinition mapOperation(Operation o) {

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());
        operation.setConversationSequence(o.getConversationSequence());
        Type returnType = o.getOutputType().getPhysical();
        operation.setReturnType(getClassName(returnType));

        DataType<List<? extends DataType<?>>> params = o.getInputType();
        for (DataType<?> param : params.getLogical()) {
            Type paramType = param.getPhysical();
            operation.addParameter(getClassName(paramType));
        }
        return operation;

    }

    /**
     * @see org.fabric3.fabric.model.physical.PhysicalOperationHelper#getClassName(java.lang.reflect.Type)
     */
    @SuppressWarnings("unchecked")
    public String getClassName(Type paramType) {

        // TODO this needs to be fixed
        if (paramType instanceof Class) {
            return ((Class) paramType).getName();
        } else if (paramType instanceof ParameterizedType) {
            Type type = ((ParameterizedType) paramType).getRawType();
            if (type instanceof Class) {
                return ((Class) type).getName();
            }
        } else if (paramType instanceof TypeVariable) {
            TypeVariable var = (TypeVariable) paramType;
            if (var.getBounds().length > 0 && var.getBounds()[0] instanceof Class) {
                return ((Class) var.getBounds()[0]).getName();
            } else if (var.getBounds().length > 0 && var.getBounds()[0] instanceof ParameterizedType) {
                Type actualType = ((ParameterizedType) var.getBounds()[0]).getRawType();
                if (!(actualType instanceof Class)) {
                    throw new AssertionError();
                }
                return ((Class) actualType).getName();
            }
        } else if (paramType instanceof GenericArrayType) {
            GenericArrayType var = (GenericArrayType) paramType;
            return "[L" + var.getGenericComponentType();
        }
        throw new AssertionError();

    }

}
