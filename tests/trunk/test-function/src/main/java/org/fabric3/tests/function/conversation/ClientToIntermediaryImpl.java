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

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;

import org.fabric3.api.annotation.PropagatesConversation;

/**
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
public class ClientToIntermediaryImpl implements ClientToIntermediary {
    @Reference
    @PropagatesConversation
    protected IntermediaryService service1;

    @Reference
    @PropagatesConversation
    protected IntermediaryService service2;


    public void propagateConversation() {
        service1.setValue("test");
        if (!"test".equals(service1.getValue())) {
            throw new AssertionError("Conversation state not maintained");
        }
        // service2 should point to the same ConversationalService instance as service1
        if (!"test".equals(service2.getValue())) {
            throw new AssertionError("Conversation state not propagated through intermediary");
        }
    }
}
