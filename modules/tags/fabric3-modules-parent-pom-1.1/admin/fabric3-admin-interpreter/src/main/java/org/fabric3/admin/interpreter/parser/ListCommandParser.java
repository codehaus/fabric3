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
package org.fabric3.admin.interpreter.parser;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.ParseException;
import org.fabric3.admin.interpreter.command.ListCommand;

/**
 * @version $Revision$ $Date$
 */
public class ListCommandParser implements CommandParser {
    private DomainController controller;

    public ListCommandParser(DomainController controller) {
        this.controller = controller;
    }

    public String getUsage() {
        return "list (ls): List deployed components.\n" +
                "usage: list <path> [-u username -p password]";
    }

    public Command parse(String[] tokens) throws ParseException {
        if (tokens.length != 0 && tokens.length != 1 && tokens.length != 5) {
            throw new ParseException("Illegal number of arguments");
        }
        ListCommand command = new ListCommand(controller);
        if (tokens.length == 0) {
            command.setPath("/");
            return command;
        }
        command.setPath(tokens[0]);
        if (tokens.length == 5) {
            ParserHelper.parseAuthorization(command, tokens, 1);
        }
        return command;
    }

}