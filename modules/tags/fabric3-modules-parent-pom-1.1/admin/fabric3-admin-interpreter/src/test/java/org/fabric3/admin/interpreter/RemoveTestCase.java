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
package org.fabric3.admin.interpreter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.admin.api.DomainController;

/**
 * @version $Revision$ $Date$
 */
public class RemoveTestCase extends TestCase {

    public void testRemove() throws Exception {
        DomainController controller = EasyMock.createMock(DomainController.class);
        controller.setUsername("username");
        controller.setPassword("password");
        EasyMock.expect(controller.isConnected()).andReturn(true);
        URI uri = URI.create("foo.jar");
        controller.uninstall(uri);
        controller.remove(uri);
        EasyMock.replay(controller);

        Interpreter interpreter = new InterpreterImpl(controller);

        InputStream in = new ByteArrayInputStream("uninstall foo.jar -u username -p password \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(controller);
    }

}