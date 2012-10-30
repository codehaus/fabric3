package org.fabric3.tests.messaging;

import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.OneWay;

/**
 * @version $Rev$ $Date$
 */
@Callback(CallbackService.class)
public interface TestCallbackService {

    @OneWay
    void invoke(Message message);

}
