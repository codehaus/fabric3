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
package org.fabric3.spi.binding.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.fabric3.spi.binding.format.UnsupportedTypesException;
import org.fabric3.spi.classloader.ClassLoaderObjectInputStream;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.util.Base64;

/**
 * MessageEncoder that uses JDK serialization to encode a message in "unwrapped" form. An "unwrapped" message is used for wire formats that encode
 * message headers as part of the transport "packet". For example, the OASIS HTTP and JMS bindings require the default wire format to encode message
 * header information as HTTP headers and JMS message properties respectively. This base implementation uses an EncodeCallback to pass serialed header
 * information to the client to encode as part of the transport packet.
 *
 * @version $Revision$ $Date$
 */
public class JDKUnwrappedMessageEncoder implements MessageEncoder {


    public String encodeText(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        callback.encodeOperationHeader(operationName);
        String routing = Base64.encode(serialize(message.getWorkContext().getCallFrameStack()));
        callback.encodeRoutingHeader(routing);
        if (!(message.getBody() instanceof String)) {
            throw new UnsupportedOperationException("Message body must be serialized as a String");
        }
        String body = (String) message.getBody();
        callback.encodeContentLengthHeader(body.length());
        return body;
    }

    public byte[] encodeBytes(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        callback.encodeOperationHeader(operationName);
        String routing = Base64.encode(serialize(message.getWorkContext().getCallFrameStack()));
        callback.encodeRoutingHeader(routing);
        if (!(message.getBody() instanceof byte[])) {
            throw new UnsupportedOperationException("Message body must be serialized as a byte[]");
        }
        byte[] body = (byte[]) message.getBody();
        callback.encodeContentLengthHeader(body.length);
        return body;
    }

    public String encodeResponseText(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException {
        if (!(message.getBody() instanceof String)) {
            throw new UnsupportedOperationException("Message body must be serialized as a String");
        }
        String body = (String) message.getBody();
        callback.encodeContentLengthHeader(body.length());
        return body;
    }

    public byte[] encodeResponseBytes(String operationName, Message message, ResponseEncodeCallback callback)
            throws EncoderException {
        if (!(message.getBody() instanceof byte[])) {
            throw new UnsupportedOperationException("Message body must be serialized as a byte[]");
        }
        byte[] body = (byte[]) message.getBody();
        callback.encodeContentLengthHeader(body.length);
        return body;
    }

    @SuppressWarnings({"unchecked"})
    public Message decode(byte[] serialized, HeaderContext context) throws EncoderException {
        Message message = new MessageImpl();
        WorkContext workContext = new WorkContext();
        message.setWorkContext(workContext);
        // set the contents as the message body
        message.setBody(serialized);
        List<CallFrame> frames = deserialize(List.class, context.getRoutingBytes());
        workContext.addCallFrames(frames);
        return message;
    }

    @SuppressWarnings({"unchecked"})
    public Message decode(String serialized, HeaderContext context) throws EncoderException {
        Message message = new MessageImpl();
        WorkContext workContext = new WorkContext();
        message.setWorkContext(workContext);
        // set the contents as the message body
        message.setBody(serialized);
        List<CallFrame> frames = deserialize(List.class, Base64.decode(context.getRoutingText()));
        workContext.addCallFrames(frames);
        return message;
    }

    public Message decodeResponse(byte[] serialized) throws EncoderException {
        Message message = new MessageImpl();
        WorkContext workContext = new WorkContext();
        message.setWorkContext(workContext);
        // set the contents as the message body
        message.setBody(serialized);
        return message;
    }

    public Message decodeResponse(String serialized) throws EncoderException {
        Message message = new MessageImpl();
        WorkContext workContext = new WorkContext();
        message.setWorkContext(workContext);
        // set the contents as the message body
        message.setBody(serialized);
        return message;
    }

    public Message decodeFault(byte[] serialized) throws EncoderException {
        Message message = new MessageImpl();
        WorkContext workContext = new WorkContext();
        message.setWorkContext(workContext);
        // set the contents as the message body
        message.setBodyWithFault(serialized);
        return message;
    }

    public Message decodeFault(String serialized) throws EncoderException {
        Message message = new MessageImpl();
        WorkContext workContext = new WorkContext();
        message.setWorkContext(workContext);
        // set the contents as the message body
        message.setBodyWithFault(serialized);
        return message;
    }

    private byte[] serialize(Object o) throws EncoderException {
        ObjectOutputStream stream = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(bos);
            stream.writeObject(o);
            stream.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new EncoderException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private <T> T deserialize(Class<T> clazz, Object object) throws EncoderException {
        ByteArrayInputStream bis = null;
        ObjectInputStream stream = null;
        try {
            boolean isString = String.class.equals(clazz);
            if (!Byte.TYPE.equals(object.getClass().getComponentType()) && isString) {
                throw new UnsupportedTypesException("This implementation only supports serialization from bytes or Strings");
            }
            byte[] bytes;
            if (isString) {
                bytes = Base64.decode((String) object);
            } else {
                bytes = (byte[]) object;
            }
            bis = new ByteArrayInputStream(bytes);
            stream = new ClassLoaderObjectInputStream(bis, this.getClass().getClassLoader());
            return clazz.cast(stream.readObject());
        } catch (IOException e) {
            throw new EncoderException(e);
        } catch (ClassNotFoundException e) {
            throw new EncoderException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}