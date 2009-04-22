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
package org.fabric3.json.runtime;

import java.net.URI;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.util.ParamTypes;
import org.fabric3.json.provision.JsonServiceInterceptorDefinition;

/**
 * Creates JsonServiceInterceptor instances.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JsonServiceInterceptorBuilder implements InterceptorBuilder<JsonServiceInterceptorDefinition, JsonServiceInterceptor> {
    private ClassLoaderRegistry registry;
    private ObjectMapper mapper = new ObjectMapper();

    public JsonServiceInterceptorBuilder(@Reference ClassLoaderRegistry registry) {
        this.registry = registry;
    }

    public JsonServiceInterceptor build(JsonServiceInterceptorDefinition definition) throws BuilderException {
        URI classloaderId = definition.getWireClassLoaderId();
        ClassLoader loader = registry.getClassLoader(classloaderId);
        List<String> list = definition.getParameterTypes();
        Class<?> paramType;
        if (list.isEmpty()) {
            paramType = Void.class;
        } else {
            try {
                // TODO support multiple params
                String name = list.get(0);
                paramType = ParamTypes.PRIMITIVES_TYPES.get(name);
                if (paramType == null) {
                    paramType = loader.loadClass(name);
                }
            } catch (ClassNotFoundException e) {
                throw new OperationTypeNotFoudException(e);
            }
        }

        return new JsonServiceInterceptor(mapper, paramType);
    }
}
