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
public class ConversationalToCompositeClientServiceImpl implements ConversationalClientService, CallbackService {
    private int count;
    private CallbackData data;

    @Reference
    protected CompositeService compositeService;

    public void invoke(CallbackData data) {
        count++;
        this.data = data;
        compositeService.invoke();
    }

    public int getCount() {
        return count;
    }

    public void onCallback() {
        count++;
        compositeService.invoke2();
    }

    public void end() {
        if (!data.isError()) {
            count++;
            data.callback();
        }
        data.getLatch().countDown();
    }
}