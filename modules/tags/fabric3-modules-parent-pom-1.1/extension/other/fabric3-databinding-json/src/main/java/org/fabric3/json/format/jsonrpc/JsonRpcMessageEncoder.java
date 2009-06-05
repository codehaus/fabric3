/*
 * Fabric3
 * Copyright © 2008-2009 Metaform Systems Limited
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
package org.fabric3.json.format.jsonrpc;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;

import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.HeaderContext;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ResponseEncodeCallback;
import org.fabric3.spi.invocation.Message;

/**
 * Serializer that reads and writes data using JSON RPC.
 * <p>
 * <b> Note this implementation is note yet complete.
 *
 * @version $Revision$ $Date$
 */
public class JsonRpcMessageEncoder implements MessageEncoder {
    private ObjectMapper mapper;

    public JsonRpcMessageEncoder() throws EncoderException {
        this.mapper = new ObjectMapper();
    }

    public String encodeText(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        StringWriter writer = new StringWriter();
        try {
            JsonRpcRequest request;
            Object[] params = (Object[]) message.getBody();
            if (params == null) {
                request = new JsonRpcRequest(UUID.randomUUID().toString(), operationName);
            } else {
                List<Object> paramArray = Arrays.asList(params);
                request = new JsonRpcRequest(UUID.randomUUID().toString(), operationName, paramArray);
            }
            mapper.writeValue(writer, request);
        } catch (IOException e) {
            throw new EncoderException(e);
        }

        return writer.toString();
    }

    public byte[] encodeBytes(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public String encodeResponseText(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException {
        StringWriter writer = new StringWriter();
        // FIXME the correlation id should be the id of the original request
        String correlationId = "";
        try {
            JsonRpcResponse request;
            Object result = message.getBody();
            if (result == null) {
                request = new JsonRpcResponse(correlationId);
            } else {
                request = new JsonRpcResponse(correlationId, result);
            }
            mapper.writeValue(writer, request);
        } catch (IOException e) {
            throw new EncoderException(e);
        }

        return writer.toString();
    }

    public byte[] encodeResponseBytes(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Message decode(byte[] encoded, HeaderContext context) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Message decode(String encoded, HeaderContext context) throws EncoderException {
        try {
            JsonRpcRequest request = mapper.readValue(encoded, JsonRpcRequest.class);
            return null;
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public Message decodeResponse(byte[] encoded) throws EncoderException {
        return null;
    }

    public Message decodeResponse(String encoded) throws EncoderException {
        return null;
    }

    public Message decodeFault(byte[] encoded) throws EncoderException {
        return null;
    }

    public Message decodeFault(String encoded) throws EncoderException {
        return null;
    }
}