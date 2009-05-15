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
package org.fabric3.json.serializer;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.UnsupportedTypesException;
import org.fabric3.spi.invocation.Message;
import org.fabric3.json.format.ApplicationFault;

/**
 * Serializer that reads and writes data using JSON. Note this implementation only encodes type information for faults, wrapping them in {@link
 * ApplicationFault}.
 *
 * @version $Revision$ $Date$
 */
public class JsonSerializer implements Serializer {
    private ObjectMapper mapper;
    private Class<?> inType;
    private Map<String, Constructor<?>> faultCtors;

    public JsonSerializer(Set<Class<?>> types, Set<Class<?>> faultTypes) throws EncoderException {
        if (types.size() > 1) {
            throw new UnsupportedTypesException("Only single parameters are supported");
        } else if (types.size() == 1) {
            inType = types.iterator().next();
        } else {
            inType = Void.class;
        }
        faultCtors = new HashMap<String, Constructor<?>>();
        for (Class<?> faultType : faultTypes) {
            try {
                if (faultType.isAssignableFrom(Throwable.class)) {
                    throw new IllegalArgumentException("Fault must be a Throwable: " + faultType.getName());
                }
                Constructor<?> ctor = faultType.getConstructor(String.class);
                faultCtors.put(faultType.getSimpleName(), ctor);
            } catch (NoSuchMethodException e) {
                throw new EncoderException("Fault type must contain a public constructor taking a String message: " + faultType.getName());
            }
        }
        this.mapper = new ObjectMapper();
    }

    public <T> T serialize(Class<T> clazz, Object message) throws EncoderException {
        if (!String.class.equals(clazz)) {
            throw new UnsupportedTypesException("This implementation only supports serialization to Strings");
        }
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, message);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        return clazz.cast(writer.toString());
    }

    public <T> T serializeResponse(Class<T> clazz, Object message) throws EncoderException {
        return serialize(clazz, message);
    }

    public <T> T serializeFault(Class<T> clazz, Throwable exception) throws EncoderException {
        ApplicationFault fault = new ApplicationFault();
        fault.setMessage(exception.getMessage());
        fault.setType(exception.getClass().getSimpleName());
        return serialize(clazz, fault);
    }

    public <T> T deserialize(Class<T> clazz, Object serialized) throws EncoderException {
        if (!String.class.equals(serialized.getClass())) {
            throw new UnsupportedTypesException("This implementation only supports serialization from Strings");
        }
        try {
            if (!Void.class.equals(inType) && !clazz.isAssignableFrom(inType) && !inType.isPrimitive()) {
                throw new EncoderException("Invalid expected type: " + clazz.getName() + ". Type must be compatible with " + inType);
            }
            return clazz.cast(mapper.readValue((String) serialized, inType));
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public <T> T deserializeResponse(Class<T> clazz, Object object) throws EncoderException {
        return deserialize(clazz, object);
    }

    public Throwable deserializeFault(Object serialized) throws EncoderException {
        if (!String.class.equals(serialized.getClass())) {
            throw new UnsupportedTypesException("This implementation only supports serialization from Strings");
        }
        ApplicationFault fault;
        try {
            fault = mapper.readValue((String) serialized, ApplicationFault.class);
        } catch (IOException e) {
            throw new EncoderException(e);
        }

        Constructor<?> ctor = faultCtors.get(fault.getType());

        if (ctor == null) {
            return new ServiceRuntimeException("Unknown fault thrown by service. Type is " + fault.getType() + ". Message:" + fault.getMessage());
        }

        try {
            return (Throwable) ctor.newInstance(fault.getMessage());
        } catch (InstantiationException e) {
            throw new EncoderException(e);
        } catch (IllegalAccessException e) {
            throw new EncoderException(e);
        } catch (InvocationTargetException e) {
            throw new EncoderException(e);
        }
    }

    public Message deserializeMessage(Object serialized) throws EncoderException {
        throw new UnsupportedOperationException();
    }
}
