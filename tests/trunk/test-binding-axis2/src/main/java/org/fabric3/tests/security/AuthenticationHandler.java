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
package org.fabric3.tests.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

/**
 * @version $Revision: 6208 $ $Date: 2008-12-07 18:29:58 +0000 (Sun, 07 Dec 2008) $
 */
public class AuthenticationHandler implements CallbackHandler {
	
	

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
               
                WSPasswordCallback ps = (WSPasswordCallback)callback;
                if (!(ps.getIdentifer().equals("Fred") && ps.getPassword().equals("changeit"))){
                  throw new UnsupportedCallbackException(callback, "Invalid user and Password");
                }
            }
        }
    }

}
