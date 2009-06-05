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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.ParseException;
import org.fabric3.admin.interpreter.command.DeployCommand;

/**
 * @version $Revision$ $Date$
 */
public class DeployCommandParser implements CommandParser {
    private DomainController controller;

    public DeployCommandParser(DomainController controller) {
        this.controller = controller;
    }

    public String getUsage() {
        return "deploy (de): Deploy an installed contribution.\n" +
                "usage: deploy <contribution> [<plan>|-plan <plan file>] [-u username -p password]";
    }

    public Command parse(String[] tokens) throws ParseException {
        if (tokens.length != 1 && tokens.length != 2 && tokens.length != 3 && tokens.length != 5 && tokens.length != 6 && tokens.length != 7) {
            throw new ParseException("Illegal number of arguments");
        }
        DeployCommand command = new DeployCommand(controller);
        try {
            command.setContributionUri(new URI(tokens[0]));
        } catch (URISyntaxException e) {
            throw new ParseException("Invalid contribution name", e);
        }
        if (tokens.length == 1) {
            return command;
        }
        if ("-plan".equals(tokens[1])) {
            try {
                URL url = ParserHelper.parseUrl(tokens[2]);
                command.setPlanFile(url);
                if (tokens.length == 7) {
                    ParserHelper.parseAuthorization(command, tokens, 3);
                }
            } catch (MalformedURLException e) {
                throw new ParseException("Invalid plan URL", e);
            }
        } else {
            if (tokens.length == 5) {
                ParserHelper.parseAuthorization(command, tokens, 1);
            } else {
                command.setPlanName(tokens[1]);
                if (tokens.length == 6) {
                    ParserHelper.parseAuthorization(command, tokens, 2);
                }
            }
        }
        return command;
    }

}