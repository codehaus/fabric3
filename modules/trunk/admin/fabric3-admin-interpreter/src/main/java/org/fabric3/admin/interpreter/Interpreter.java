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

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @version $Revision$ $Date$
 */
public interface Interpreter {

    /**
     * Processes an instruction.
     *
     * @param command the instruction
     * @param out     the PrintStream where command output is sent
     * @throws InterpreterException if an error occurs processing the instruction
     */
    public void process(String command, PrintStream out) throws InterpreterException;

    /**
     * Provides an interactive command prompt for issuing commands to the DomainController.
     *
     * @param in  the InputStream where instructions are received
     * @param out the PrintStream where command output is sent
     * @throws InterpreterException if an error occurs processing an instruction
     */
    void processInteractive(InputStream in, PrintStream out) throws InterpreterException;

}
