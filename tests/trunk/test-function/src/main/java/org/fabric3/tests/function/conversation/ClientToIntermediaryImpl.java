package org.fabric3.tests.function.conversation;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;

/**
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
public class ClientToIntermediaryImpl implements ClientToIntermediary {
    @Reference
    protected IntermediaryService service1;

    @Reference
    protected IntermediaryService service2;


    public void propagateConversation() {
        service1.setValue("test");
        if (!"test".equals(service1.getValue())) {
            throw new AssertionError("Conversation state not maintained");
        }
        // service2 should point to the same ConversationalService instance as service1
        if (!"test".equals(service1.getValue())) {
            throw new AssertionError("Conversation state not propagated through intermediary");
        }
    }
}
