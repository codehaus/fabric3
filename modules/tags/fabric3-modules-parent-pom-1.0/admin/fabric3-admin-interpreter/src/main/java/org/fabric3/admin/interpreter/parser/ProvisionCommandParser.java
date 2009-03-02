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
import java.net.URL;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.ParseException;
import org.fabric3.admin.interpreter.command.ProvisionCommand;

/**
 * @version $Revision$ $Date$
 */
public class ProvisionCommandParser implements CommandParser {
    private DomainController controller;

    public ProvisionCommandParser(DomainController controller) {
        this.controller = controller;
    }

    public String getUsage() {
        return "provision (pr): Stores, installs, and deploys a contribution.\n" +
                "usage: provision <contribution file> <plan name>|-plan <plan file> [-u username -p password]";
    }

    public Command parse(String[] tokens) throws ParseException {
        if (tokens.length != 1 && tokens.length != 2 && tokens.length != 3 && tokens.length != 5 && tokens.length != 6 && tokens.length != 7) {
            throw new ParseException("Illegal number of arguments");
        }
        ProvisionCommand command = new ProvisionCommand(controller);
        try {
            URL contributionUrl = ParserHelper.parseUrl(tokens[0]);
            command.setContribution(contributionUrl);
            if (tokens.length == 1) {
                return command;
            }
        } catch (MalformedURLException e) {
            throw new ParseException("Invalid contribution URL", e);
        }
        if ("-plan".equals(tokens[1])) {
            try {
                URL url = ParserHelper.parseUrl(tokens[2]);
                command.setPlanFile(url);
            } catch (MalformedURLException e) {
                throw new ParseException("Invalid plan URL", e);
            }
            if (tokens.length == 7) {
                ParserHelper.parseAuthorization(command, tokens, 3);
            }
        } else {
            if (tokens.length == 5) {
                ParserHelper.parseAuthorization(command, tokens, 1);
            } else if (tokens.length == 6) {
                command.setPlanName(tokens[1]);
                ParserHelper.parseAuthorization(command, tokens, 2);
            } else {
                if (tokens.length == 6) {
                    ParserHelper.parseAuthorization(command, tokens, 2);
                }
            }
        }
        return command;
    }

}
