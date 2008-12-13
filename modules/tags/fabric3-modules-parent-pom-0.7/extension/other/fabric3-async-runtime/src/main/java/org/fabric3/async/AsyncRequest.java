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
package org.fabric3.async;

import java.util.List;
import java.util.Map;

import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Encapsulates an invocation to be processed asynchronously.
 *
 * @version $Revision$ $Date$
 */
public class AsyncRequest extends DefaultPausableWork {
    private final Interceptor next;
    private final Message message;
    private List<CallFrame> stack;
    private Map<String, Object> headers;

    public AsyncRequest(Interceptor next, Message message, List<CallFrame> stack, Map<String, Object> headers) {
        this.next = next;
        this.message = message;
        this.stack = stack;
        this.headers = headers;
    }

    public void execute() {
        WorkContext newWorkContext = new WorkContext();
        if (stack != null) {
            newWorkContext.addCallFrames(stack);
        }
        if (headers != null) {
            newWorkContext.addHeaders(headers);
        }
        message.setWorkContext(newWorkContext);
        next.invoke(message);
    }

    public Interceptor getNext() {
        return next;
    }

    public Message getMessage() {
        return message;
    }
}
