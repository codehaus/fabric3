package org.fabric3.tests.function.callback.conversation;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

import org.fabric3.tests.function.callback.common.CallbackData;

/**
 * @version $Revision$ $Date$
 */
@Service(interfaces = {ConversationalClientService.class, CallbackService.class})
@Scope("CONVERSATION")
public class ConversationalClientServiceImpl implements ConversationalClientService, CallbackService {
    private int count;
    private CallbackData data;

    @Reference
    protected ForwardService forwardService;

    public void invoke(CallbackData data) {
        count++;
        this.data = data;
        forwardService.invokeForward();
    }

    public int getCount() {
        return count;
    }

    public void onCallback() {
        count++;
        if (forwardService.getCount() != 1) {
            throw new AssertionError("Forward service count incorrect");
        }
        forwardService.invokeForward2();
    }

    public void end() {
        if (forwardService.getCount() != 2) {
            //noinspection ThrowableInstanceNeverThrown
            AssertionError e = new AssertionError("Forward service count incorrect");
            data.setException(e);
        } else {
            count++;
            data.callback();
        }
        data.getLatch().countDown();
    }
}
