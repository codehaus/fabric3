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
package org.fabric3.tests.function.conversation;

import org.oasisopen.sca.annotation.Scope;
import org.osoa.sca.annotations.ConversationID;
import org.osoa.sca.annotations.EndsConversation;

/**
 * @version $Rev$ $Date$
 */
@Scope("CONVERSATION")
public class ConversationalServiceImpl implements ConversationalService {
    private String value;
    private Object conversationId;

//    @ConversationID
//    protected Object fieldConversationId;

    // FIXME the introspection framwork does not support injecting context types (RequestContext, ComponentContext) and conversation ids on multiple
    // sites.  

    @ConversationID
    public void setConversationId(Object conversationId) {
        this.conversationId = conversationId;
    }

    public Object getConversationId() {
        return conversationId;
    }

    public Object getFieldConversationId() {
        return null;
        //return fieldConversationId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @EndsConversation
    public String end() {
        return value;
    }
}
