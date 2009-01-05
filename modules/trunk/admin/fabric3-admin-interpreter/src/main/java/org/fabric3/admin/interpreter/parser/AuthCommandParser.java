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

import org.fabric3.admin.api.DomainController;
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

    public String getUsage() {
        return "authenticate (au): Set authentication credentials.\n" +
                "usage: authenticate -u username -p password";
    }

    public Command parse(String[] tokens) throws ParseException {
        if (tokens.length != 4) {
            throw new ParseException("Illegal number of arguments");
        }
        AuthCommand command = new AuthCommand(controller);
        ParserHelper.parseAuthorization(command, tokens, 0);
        return command;
    }

}