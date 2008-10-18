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

import java.util.Iterator;

import org.antlr.runtime.Token;

/**
 * Parses command tokens and their children in the AST generated from a set of instructions given to the Interpeter.
 *
 * @version $Revision$ $Date$
 */
public interface CommandParser {

    /**
     * Walk the AST starting with the command token this parser handles. When this method returns, the CommandParser must position the token stream on
     * the UP token after all children have been iterated.
     *
     * @param iterator the AST iterator
     * @return the parsed Command object
     * @throws ParseException if a parse error is encountered
     */
    Command parse(Iterator<Token> iterator) throws ParseException;
}
