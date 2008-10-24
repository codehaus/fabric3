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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.antlr.runtime.Token;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.cli.DomainAdminLexer;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.ParseException;
import org.fabric3.admin.interpreter.command.InstallCommand;

/**
 * @version $Revision$ $Date$
 */
public class InstallCommandParser implements CommandParser {
    private DomainController controller;

    public InstallCommandParser(DomainController controller) {
        this.controller = controller;
    }

    public Command parse(Iterator<Token> iterator) throws ParseException {
        Token token = iterator.next();
        InstallCommand command = new InstallCommand(controller);
        while (Token.UP != token.getType()) {
            switch (token.getType()) {
            case DomainAdminLexer.FILE:
                parseFile(command, iterator);
                break;
            case DomainAdminLexer.PARAMETER:
                parseParameter(command, iterator);
                break;
            default:
                throw new AssertionError("Invalid token: " + token.getText());
            }
            token = iterator.next();
        }
        if (command.getContribution() == null) {
            throw new AssertionError("FILE token not found");
        }
        return command;
    }

    private void parseParameter(InstallCommand command, Iterator<Token> iterator) {
        // proceed past DOWN;
        iterator.next();
        Token token = iterator.next();
        switch (token.getType()) {
        case DomainAdminLexer.PARAM_USERNAME:
            command.setUsername(iterator.next().getText());
            break;
        case DomainAdminLexer.PARAM_PASSWORD:
            command.setPassword(iterator.next().getText());
            break;
        case DomainAdminLexer.PARAM_CONTRIBUTION_NAME:
            command.setContributionName(iterator.next().getText());
            break;
        default:
            throw new AssertionError("Invalid parameter token type: " + token.getText());
        }
        // proceed past UP
        iterator.next();
    }

    private void parseFile(InstallCommand command, Iterator<Token> iterator) throws ParseException {
        // proceed past DOWN;
        iterator.next();
        String text = iterator.next().getText();
        // proceed past UP
        iterator.next();
        try {
            URL contribution;
            if (!text.contains("://")) {
                // assume it is a file
                contribution = new File(text).toURI().toURL();
            } else {
                contribution = new URL(text);
            }
            command.setContribution(contribution);
        } catch (MalformedURLException e) {
            throw new ParseException("Invalid contribution URL", e);
        }
    }

}
