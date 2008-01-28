/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.binding.ws.axis2.databinding;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;

/**
 * @version $Revision$ $Date$
 */
public class JaxbInterceptor implements Interceptor {
    
    private Interceptor next;

    public Interceptor getNext() {
        // TODO Auto-generated method stub
        return next;
    }

    public Message invoke(Message message) {
        // TODO Auto-generated method stub
        return message;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
