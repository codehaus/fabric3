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
import org.fabric3.admin.interpreter.Settings;
import org.fabric3.admin.interpreter.command.UseCommand;

/**
 * @version $Revision$ $Date$
 */
public class UseCommandParser implements CommandParser {
    private DomainController controller;
    private Settings settings;

    public UseCommandParser(DomainController controller, Settings settings) {
        this.controller = controller;
        this.settings = settings;
    }

    public Command parse(Iterator<Token> iterator) throws ParseException {
        Token token = iterator.next();
        UseCommand command = new UseCommand(controller);
        while (Token.UP != token.getType()) {
            switch (token.getType()) {
            case DomainAdminLexer.PARAM_DOMAIN_NAME:
                // proceed past DOWN;
                iterator.next();
                String domain = iterator.next().getText();
                String address = settings.getDomainAddress(domain);
                if (address == null) {
                    throw new UnknownDomainException("The domain has not been configured: " + domain);
                }
                command.setDomainAddress(address);

                // proceed past UP
                iterator.next();
                break;
            default:
                throw new AssertionError("Invalid token: " + token.getText());
            }
            token = iterator.next();
        }
        return command;
    }


}