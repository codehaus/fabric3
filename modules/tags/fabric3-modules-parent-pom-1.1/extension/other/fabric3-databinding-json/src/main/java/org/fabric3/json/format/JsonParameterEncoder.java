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
package org.fabric3.json.format;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.binding.format.AbstractParameterEncoder;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.invocation.Message;

/**
 * ParameterEncoder that uses JSON. Note this implementation only encodes type information for faults, wrapping them in {@link ApplicationFault}.
 *
 * @version $Revision$ $Date$
 */
public class JsonParameterEncoder extends AbstractParameterEncoder {
    private ObjectMapper mapper;
    private Map<String, OperationTypes> mappings;
    private Map<String, Constructor<?>> faultCtors;

    public JsonParameterEncoder(Map<String, OperationTypes> mappings) throws EncoderException {
        this.mappings = mappings;
        this.mapper = new ObjectMapper();
        faultCtors = new HashMap<String, Constructor<?>>();
        for (OperationTypes types : mappings.values()) {
            for (Class<?> faultType : types.getFaultTypes()) {
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
        }
    }


    public String encodeText(Message message) throws EncoderException {
        Object body = message.getBody();
        if (message.isFault()) {
            Throwable exception = (Throwable) message.getBody();
            ApplicationFault fault = new ApplicationFault();
            fault.setMessage(exception.getMessage());
            fault.setType(exception.getClass().getSimpleName());
            body = fault;
        } else {
            if (body != null && body.getClass().isArray() && !body.getClass().isPrimitive()) {
                Object[] payload = (Object[]) body;
                if (payload.length > 1) {
                    throw new UnsupportedOperationException("Multiple paramters not supported");
                }
                body = payload[0];
            }
        }
        try {
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, body);
            return writer.toString();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public byte[] encodeBytes(Message message) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Object decode(String operationName, String serialized) throws EncoderException {
        try {
            OperationTypes types = mappings.get(operationName);
            if (types == null) {
                throw new EncoderException("Operation not found: " + operationName);
            }

            Class<?> inType = types.getInParameterType();
            if (inType != null) {
                return mapper.readValue(serialized, inType);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        }

    }

    public Object decodeResponse(String operationName, String serialized) throws EncoderException {
        try {
            OperationTypes types = mappings.get(operationName);
            if (types == null) {
                throw new EncoderException("Operation not found: " + operationName);
            }
            Class<?> inType = types.getInParameterType();
            if (inType != null) {
                return mapper.readValue(serialized, inType);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public Throwable decodeFault(String operationName, String serialized) throws EncoderException {
        ApplicationFault fault;
        try {
            fault = mapper.readValue(serialized, ApplicationFault.class);
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


}