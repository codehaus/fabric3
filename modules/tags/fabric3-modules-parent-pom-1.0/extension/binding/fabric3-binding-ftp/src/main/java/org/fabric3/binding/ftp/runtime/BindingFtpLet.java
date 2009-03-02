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
package org.fabric3.binding.ftp.runtime;

import java.io.InputStream;

import org.fabric3.api.ftp.FtpConstants;
import org.fabric3.api.ftp.FtpLet;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Wire;

/**
 * Handles incoming FTP puts from the protocol stack.
 *
 * @version $Revision$ $Date$
 */
public class BindingFtpLet implements FtpLet {
    private String servicePath;
    private Wire wire;
    private Interceptor interceptor;
    private BindingMonitor monitor;

    public BindingFtpLet(String servicePath, Wire wire, BindingMonitor monitor) {
        this.servicePath = servicePath;
        this.wire = wire;
        this.monitor = monitor;
    }

    public boolean onUpload(String fileName, String contentType, InputStream uploadData) throws Exception {
        Object[] args = new Object[]{fileName, uploadData};
        WorkContext workContext = new WorkContext();
        // set the header value for the request context
        workContext.setHeader(FtpConstants.HEADER_CONTENT_TYPE, contentType);
        Message input = new MessageImpl(args, false, workContext);
        Message msg = getInterceptor().invoke(input);
        if (msg.isFault()) {
            monitor.fileProcessingError(servicePath, (Throwable) msg.getBody());
            return false;
        }
        return true;
    }

    private Interceptor getInterceptor() {
        // lazy load the interceptor as it may not have been added when the instance was created in the wire attacher
        if (interceptor == null) {
            interceptor = wire.getInvocationChains().values().iterator().next().getHeadInterceptor();
        }
        return interceptor;
    }
}
