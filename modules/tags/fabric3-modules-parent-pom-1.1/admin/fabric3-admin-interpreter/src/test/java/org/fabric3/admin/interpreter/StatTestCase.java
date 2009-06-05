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
package org.fabric3.admin.interpreter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.admin.api.DomainController;
import org.fabric3.management.contribution.ContributionInfo;

/**
 * @version $Revision$ $Date$
 */
public class StatTestCase extends TestCase {

    public void testListWithAuth() throws Exception {
        DomainController controller = EasyMock.createMock(DomainController.class);
        controller.setUsername("username");
        controller.setPassword("password");
        EasyMock.expect(controller.isConnected()).andReturn(true);
        EasyMock.expect(controller.stat()).andReturn(Collections.<ContributionInfo>emptySet());
        EasyMock.replay(controller);

        Interpreter interpreter = new InterpreterImpl(controller);

        InputStream in = new ByteArrayInputStream("st -u username -p password \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(controller);
    }

    public void testListWithNoAuth() throws Exception {
        DomainController controller = EasyMock.createMock(DomainController.class);
        EasyMock.expect(controller.isConnected()).andReturn(true);
        EasyMock.expect(controller.stat()).andReturn(Collections.<ContributionInfo>emptySet());
        EasyMock.replay(controller);

        Interpreter interpreter = new InterpreterImpl(controller);

        InputStream in = new ByteArrayInputStream("st \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(controller);
    }


    public void testFullCommand() throws Exception {
        DomainController controller = EasyMock.createMock(DomainController.class);
        EasyMock.expect(controller.isConnected()).andReturn(true);
        EasyMock.expect(controller.stat()).andReturn(Collections.<ContributionInfo>emptySet());
        EasyMock.replay(controller);

        Interpreter interpreter = new InterpreterImpl(controller);

        InputStream in = new ByteArrayInputStream("status \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(controller);
    }

}