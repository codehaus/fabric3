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
package org.fabric3.json;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.util.ParamTypes;

/**
 * Creates JsonReferenceInterceptor instances.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JsonReferenceInterceptorBuilder implements InterceptorBuilder<JsonReferenceInterceptorDefinition, JsonReferenceInterceptor> {
    private ClassLoaderRegistry registry;
    private ObjectMapper mapper = new ObjectMapper();

    public JsonReferenceInterceptorBuilder(@Reference ClassLoaderRegistry registry) {
        this.registry = registry;
    }

    public JsonReferenceInterceptor build(JsonReferenceInterceptorDefinition definition) throws BuilderException {
        URI classloaderId = definition.getWireClassLoaderId();
        ClassLoader loader = registry.getClassLoader(classloaderId);
        List<String> list = definition.getFaultTypes();
        List<Class<?>> paramTypes = new ArrayList<Class<?>>();
        try {
            String returnTypeName = definition.getReturnType();
            Class<?> returnType = ParamTypes.PRIMITIVES_TYPES.get(returnTypeName);
            if (returnType == null) {
                returnType = loader.loadClass(returnTypeName);
            }
            if (!list.isEmpty()) {
                Class<?> paramType;
                String name = list.get(0);
                paramType = ParamTypes.PRIMITIVES_TYPES.get(name);
                if (paramType == null) {
                    paramType = loader.loadClass(name);
                }
                paramTypes.add(paramType);
            }
            return new JsonReferenceInterceptor(mapper, returnType, paramTypes);
        } catch (ClassNotFoundException e) {
            throw new OperationTypeNotFoudException(e);
        }
    }
}