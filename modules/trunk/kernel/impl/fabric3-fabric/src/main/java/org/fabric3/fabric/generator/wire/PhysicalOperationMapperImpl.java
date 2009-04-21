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
package org.fabric3.fabric.generator.wire;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * @version $Revision$ $Date$
 */
public class PhysicalOperationMapperImpl implements PhysicalOperationMapper {
    private static final QName ONEWAY = new QName(SCA_NS, "oneWay");
    private static final QName OASIS_ONEWAY = new QName(Constants.SCA_NS, "oneWay");

    @SuppressWarnings({"unchecked"})
    public <T> PhysicalOperationDefinition map(Operation<T> o) {

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());
        operation.setEndsConversation(o.getConversationSequence() == Operation.CONVERSATION_END);
        if (o.getIntents().contains(ONEWAY) || o.getIntents().contains(OASIS_ONEWAY)) {
            operation.setOneWay(true);
        }
        Type returnType = o.getOutputType().getPhysical();
        operation.setReturnType(getClassName(returnType));

        for (DataType<T> fault : o.getFaultTypes()) {
            Type faultType = fault.getPhysical();
            operation.addFaultType(getClassName(faultType));
        }

        DataType<List<DataType<T>>> params = o.getInputType();
        for (DataType<?> param : params.getLogical()) {
            Type paramType = param.getPhysical();
            operation.addParameter(getClassName(paramType));
        }
        return operation;

    }

    private String getClassName(Type paramType) {

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
