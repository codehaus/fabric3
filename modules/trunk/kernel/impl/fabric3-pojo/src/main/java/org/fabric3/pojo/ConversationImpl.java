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
package org.fabric3.pojo;

import java.io.Serializable;

import org.osoa.sca.Conversation;

/**
 * Implementation of specification Conversation interface.
 *
 * @version $Rev: 2939 $ $Date: 2008-02-28 23:03:30 -0800 (Thu, 28 Feb 2008) $
 */
public class ConversationImpl implements Conversation, Serializable {
    private static final long serialVersionUID = 8249514203064252385L;
    private final Object conversationId;

    /**
     * Constructor defining the conversation id.
     *
     * @param conversationID the conversation id
     */
    public ConversationImpl(Object conversationID) {
        assert conversationID != null;
        this.conversationId = conversationID;
    }

    public Object getConversationID() {
        return conversationId;
    }

    public void end() {
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConversationImpl that = (ConversationImpl) o;
        return conversationId.equals(that.conversationId);
    }

    public int hashCode() {
        return conversationId.hashCode();
    }
}
