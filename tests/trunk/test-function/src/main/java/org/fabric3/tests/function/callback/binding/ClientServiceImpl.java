/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.tests.function.callback.binding;

import org.oasisopen.sca.annotation.OneWay;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import org.fabric3.tests.function.callback.common.CallbackData;

/**
 * @version $Revision$ $Date$
 */
@Service(interfaces = {ClientService.class, CallbackService.class})
public class ClientServiceImpl implements ClientService, CallbackService {

    @Reference
    protected ForwardService forwardService;

    public void invoke(CallbackData data) {
        forwardService.invoke(data);
    }

    @OneWay
    public void onCallback(CallbackData data) {
        data.callback();
        data.getLatch().countDown();
    }
}