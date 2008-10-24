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
package org.fabric3.admin.cli;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.impl.DomainControllerImpl;
import org.fabric3.admin.interpreter.Interpreter;
import org.fabric3.admin.interpreter.InterpreterException;
import org.fabric3.admin.interpreter.InterpreterImpl;

/**
 * Main entry point for the domain administation command line tool.
 *
 * @version $Revision$ $Date$
 */
public class Main {

    /**
     * Executes either a single instruction passed from the command line or enters into interactive mode.
     *
     * @param args the instruction to execture or an empty array
     * @throws InterpreterException if an error occurs executing an instruction or set of instructions
     */
    public static void main(String[] args) throws InterpreterException {
        DomainController controller = new DomainControllerImpl();
        Interpreter interpreter = new InterpreterImpl(controller) {
        };
        if (args.length == 0) {
            System.out.println("\nFabric3 Admininstration Interface");
            interpreter.processInteractive(System.in, System.out);
        } else {
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(" ").append(arg);
            }
            interpreter.process(builder.toString(), System.out);
        }
    }

}
