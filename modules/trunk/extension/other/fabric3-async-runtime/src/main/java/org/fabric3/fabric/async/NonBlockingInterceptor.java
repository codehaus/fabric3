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
package org.fabric3.fabric.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Adds non-blocking behavior to an invocation chain
 *
 * @version $$Rev$$ $$Date$$
 */
public class NonBlockingInterceptor implements Interceptor {

    protected static final Message RESPONSE = new ImmutableMessage();

    private final WorkScheduler workScheduler;
    private Interceptor next;

    public NonBlockingInterceptor(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    public Message invoke(final Message msg) {
        WorkContext workContext = msg.getWorkContext();
        List<CallFrame> newStack = null;
        List<CallFrame> stack = workContext.getCallFrameStack();
        if (stack != null && !stack.isEmpty()) {
            // clone the callstack to avoid multiple threads seeing changes
            newStack = new ArrayList<CallFrame>(stack);
        }
        msg.setWorkContext(null);
        Map<String, Object> newHeaders = null;
        Map<String, Object> headers = workContext.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // clone the headers to avoid multiple threads seeing changes
            newHeaders = new HashMap<String, Object>(headers);
        }
        AsyncRequest request = new AsyncRequest(next, msg, newStack, newHeaders);
        workScheduler.scheduleWork(request);
        return RESPONSE;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public boolean isOptimizable() {
        return false;
    }

    /**
     * A dummy message passed back on an invocation
     */
    private static class ImmutableMessage implements Message {

        public Object getBody() {
            return null;
        }

        public void setBody(Object body) {
            if (body != null) {
                throw new UnsupportedOperationException();
            }
        }

        public WorkContext getWorkContext() {
            throw new UnsupportedOperationException();
        }

        public void setWorkContext(WorkContext workContext) {
            throw new UnsupportedOperationException();
        }

        public boolean isFault() {
            return false;
        }

        public void setBodyWithFault(Object fault) {
            throw new UnsupportedOperationException();
        }

    }

}
