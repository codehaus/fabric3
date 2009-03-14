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
import org.fabric3.admin.interpreter.command.InstallProfileCommand;
import org.fabric3.admin.interpreter.command.StoreProfileCommand;
import org.fabric3.admin.interpreter.command.UninstallProfileCommand;

/**
 * @version $Revision$ $Date$
 */
public class ProfileCommandParser implements CommandParser {
    private DomainController controller;

    public ProfileCommandParser(DomainController controller) {
        this.controller = controller;
    }

    public String getUsage() {
        return "profile (pf): Uploads and/or installs a profile to the domain repository.\n" +
                "usage: profile store <profile file> [-u username -p password]" +
                "usage: profile install <profile> [-u username -p password]";
    }

    public Command parse(String[] tokens) throws ParseException {
        if (tokens.length != 2 && tokens.length != 6) {
            throw new ParseException("Illegal number of arguments");
        }
        if ("store".equals(tokens[0])) {
            return store(tokens);
        } else if ("install".equals(tokens[0])) {
            return install(tokens);
        } else if ("uninstall".equals(tokens[0])) {
            return uninstall(tokens);
        } else {
            throw new ParseException("Unknown profile command: " + tokens[1]);
        }
    }

    private Command uninstall(String[] tokens) throws ParseException {
        UninstallProfileCommand command = new UninstallProfileCommand(controller);
        try {
            command.setProfileUri(new URI(tokens[1]));
        } catch (URISyntaxException e) {
            throw new ParseException("Invalid profile name", e);
        }
        if (tokens.length == 6) {
            ParserHelper.parseAuthorization(command, tokens, 2);
        }
        return command;
    }

    private Command install(String[] tokens) throws ParseException {
        InstallProfileCommand command = new InstallProfileCommand(controller);
        try {
            command.setProfileUri(new URI(tokens[1]));
        } catch (URISyntaxException e) {
            throw new ParseException("Invalid profile name", e);
        }
        if (tokens.length == 6) {
            ParserHelper.parseAuthorization(command, tokens, 2);
        }
        return command;
    }

    private Command store(String[] tokens) throws ParseException {
        StoreProfileCommand command = new StoreProfileCommand(controller);
        try {
            URL url = ParserHelper.parseUrl(tokens[1]);
            command.setProfile(url);
        } catch (MalformedURLException e) {
            throw new ParseException("Invalid profile URL", e);
        }
        if (tokens.length == 6) {
            ParserHelper.parseAuthorization(command, tokens, 2);
        }
        return command;
    }

}