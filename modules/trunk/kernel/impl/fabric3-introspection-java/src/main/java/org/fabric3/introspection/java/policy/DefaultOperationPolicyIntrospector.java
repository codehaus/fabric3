package org.fabric3.introspection.java.policy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.policy.OperationPolicyIntrospector;

/**
 * Defailt implementation of OperationPolicyIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class DefaultOperationPolicyIntrospector implements OperationPolicyIntrospector {
    private PolicyAnnotationProcessor policyProcessor;

    public DefaultOperationPolicyIntrospector(@Reference PolicyAnnotationProcessor policyProcessor) {
        this.policyProcessor = policyProcessor;
    }

    public void introspectPolicyOnOperations(ServiceContract contract, Class<?> implClass, IntrospectionContext context) {
        for (Operation operation : contract.getOperations()) {
            // determine the operation signature and look up the corresponding method on the implementation class
            List<DataType<?>> types = operation.getInputTypes();
            Class<?>[] params = new Class<?>[types.size()];
            int i = 0;
            for (DataType<?> type : types) {
                Object logical = type.getLogical();
                if (!(logical instanceof Class)) {
                    // not possible
                    throw new AssertionError();
                }
                params[i] = (Class<?>) logical;
                i++;
            }
            try {
                Method method = implClass.getMethod(operation.getName(), params);
                for (Annotation annotation : method.getAnnotations()) {
                    policyProcessor.process(annotation, operation, context);
                }
            } catch (NoSuchMethodException e) {
                // should not happen
                throw new AssertionError(e);
            }
        }
    }

}
