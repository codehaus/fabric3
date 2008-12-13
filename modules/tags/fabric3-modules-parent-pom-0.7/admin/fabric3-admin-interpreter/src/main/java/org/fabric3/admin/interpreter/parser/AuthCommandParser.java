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
package org.fabric3.admin.interpreter.parser;

import java.util.Iterator;

import org.antlr.runtime.Token;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.cli.DomainAdminLexer;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.ParseException;
import org.fabric3.admin.interpreter.command.AuthCommand;

/**
 * @version $Revision$ $Date$
 */
public class AuthCommandParser implements CommandParser {
    private DomainController controller;

    public AuthCommandParser(DomainController controller) {
        this.controller = controller;
    }

    public Command parse(Iterator<Token> iterator) throws ParseException {
        Token token = iterator.next();
        AuthCommand command = new AuthCommand(controller);
        while (DomainAdminLexer.PARAMETER == token.getType()) {
            // proceed past down
            iterator.next();
            token = iterator.next();
            switch (token.getType()) {
            case DomainAdminLexer.PARAM_USERNAME:
                command.setUsername(iterator.next().getText());
                break;
            case DomainAdminLexer.PARAM_PASSWORD:
                command.setPassword(iterator.next().getText());
                break;
            default:
                throw new AssertionError("Invalid parameter token type: " + token.getText());
            }
            // proceed past up
            iterator.next();
            // move to next param
            token = iterator.next();
        }
        // proceed past UP
        iterator.next();
        return command;
    }


}