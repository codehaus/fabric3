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
package org.fabric3.policy.attachment;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Reference;

/**
 * @version $Revision$ $Date$
 */
public class AttachToImplementationTest extends TestCase {

    @Reference
    protected AttachService attachService;

    @Reference
    protected AttachService noAttachService;

    @Reference
    protected AttachService attachBindingService;


    public void testAttachToImplementationInvoke() throws Exception {
        // verify implementation attachment
        attachService.invoke();
    }

    public void testNoAttachToImplementationInvoke() throws Exception {
        // verify implementation attachment performs invalid selection
        noAttachService.invoke();
    }

    public void testAttachBindingToImplementationInvoke() throws Exception {
        // verify implementation attachment selects a binding
        attachBindingService.invoke();
    }

}
