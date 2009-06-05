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
package org.fabric3.ftp.server.codec;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.textline.TextLineDecoder;

/**
 * Protocol encoder and decoder factory for inbound and outbound messages.
 * 
 * @version $Revision$ $Date$
 */
public class CodecFactory implements ProtocolCodecFactory {
    
    private static Charset CHARSET = Charset.forName("UTF-8");
    private static CharsetEncoder CHARSET_ENCODER = CHARSET.newEncoder();
    
    private ProtocolDecoder decoder = new TextLineDecoder(CHARSET);
    private ProtocolEncoder encoder = new ResponseEncoder();

    /**
     * Gets the protocol decoder.
     * 
     * @param session Session for which the decoder is created.
     * @return Protocol decoder.
     */
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

    /**
     * Gets the protocol encoder.
     * 
     * @param session Session for which the encoder is created.
     * @return Protocol encoder.
     */
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }
    
    /*
     * Response encoder.
     */
    private class ResponseEncoder extends ProtocolEncoderAdapter {

        public void encode(IoSession session, Object message, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
            
            String stringMessage = message.toString();
            IoBuffer buffer = IoBuffer.allocate(stringMessage.length()).setAutoExpand(true);
            buffer.putString(stringMessage, CHARSET_ENCODER);

            buffer.flip();
            protocolEncoderOutput.write(buffer);
            
        }
        
    }

}
