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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.parser.AuthCommandParser;
import org.fabric3.admin.interpreter.parser.DeployCommandParser;
import org.fabric3.admin.interpreter.parser.ListCommandParser;
import org.fabric3.admin.interpreter.parser.ProfileCommandParser;
import org.fabric3.admin.interpreter.parser.ProvisionCommandParser;
import org.fabric3.admin.interpreter.parser.RemoveCommandParser;
import org.fabric3.admin.interpreter.parser.StatCommandParser;
import org.fabric3.admin.interpreter.parser.InstallCommandParser2;
import org.fabric3.admin.interpreter.parser.UndeployCommandParser;
import org.fabric3.admin.interpreter.parser.UseCommandParser;

/**
 * Default interpreter implementation. This implementation constructs a parse tree from an instruction as defined by the domain adminsitration
 * grammar. This tree is then transformed into an AST which is traversed to produce a set of commands to execute against the DomainController.
 * <p/>
 * Antlr3 is used as the parser technology to construct the parse tree and AST.
 *
 * @version $Revision$ $Date$
 */
public class InterpreterImpl implements Interpreter {
    private static final String PROMPT = "\nf3>";
    private static final String HELP = "help";
    private static final String HELP_TEXT = "Type help <subcommand> for more information: \n\n"
            + "   authenticate (au) \n"
            + "   deploy (de) \n"
            + "   install (ins) \n"
            + "   list (ls) \n"
            + "   profile (pf) \n"
            + "   provision (pr) \n"
            + "   remove (rm) \n"
            + "   status (st) \n"
            + "   undeploy (ude) \n"
            + "   uninstall (uin) \n"
            + "   use \n";

    private DomainController controller;
    private Settings settings;
    private Map<String, CommandParser> parsers;

    public InterpreterImpl(DomainController controller) {
        this(controller, new TransientSettings());
    }

    public InterpreterImpl(DomainController controller, Settings settings) {
        this.controller = controller;
        this.settings = settings;
        createParsers();
        setDefaultAddress();
    }

    public void processInteractive(InputStream in, PrintStream out) {
        Scanner scanner = new Scanner(in);
        while (true) {
            out.print(PROMPT);
            String line = scanner.nextLine().trim();
            if ("quit".equals(line) || "exit".equals(line)) break;
            try {
                process(line, out);
            } catch (InterpreterException e) {
                // TODO handle this better
                e.printStackTrace();
            }
        }
    }


    public void process(String line, PrintStream out) throws InterpreterException {
        // parse the command, strip whitespace and tokenize the command line
        line = line.trim();
        String commandString;
        String tokens[];
        int index = line.indexOf(" ");
        if (index == -1) {
            commandString = line;
            tokens = new String[0];
        } else {
            commandString = line.substring(0, index);
            String replaced = line.substring(index + 1).replaceAll("\\s{2,}", " ");
            tokens = replaced.split(" ");
        }
        if (HELP.equals(commandString)) {
            if (tokens.length == 0) {
                out.println(HELP_TEXT);
            } else {
                CommandParser parser = parsers.get(tokens[0]);
                if (parser == null) {
                    throw new InterpreterException("Unrecognized command: " + commandString);
                }
                out.println(parser.getUsage());
            }

            return;
        }
        CommandParser parser = parsers.get(commandString);
        if (parser == null) {
            throw new InterpreterException("Unrecognized command: " + commandString);
        }
        Command command = parser.parse(tokens);
        try {
            command.execute(out);
        } catch (CommandException e) {
            out.println("ERORR: An error was encountered");
            e.printStackTrace(out);
        }
    }

    /**
     * Initializes the command parsers
     */
    private void createParsers() {
        parsers = new HashMap<String, CommandParser>();
        AuthCommandParser authenticateParser = new AuthCommandParser(controller);
        parsers.put("au", authenticateParser);
        parsers.put("authenticate", authenticateParser);
        InstallCommandParser2 installParser = new InstallCommandParser2(controller);
        parsers.put("install", installParser);
        parsers.put("ins", installParser);
        StatCommandParser statusParser = new StatCommandParser(controller);
        parsers.put("status", statusParser);
        parsers.put("st", statusParser);
        DeployCommandParser deployParser = new DeployCommandParser(controller);
        parsers.put("deploy", deployParser);
        parsers.put("de", deployParser);
        UndeployCommandParser undeployParser = new UndeployCommandParser(controller);
        parsers.put("undeploy", undeployParser);
        parsers.put("ude", undeployParser);
        RemoveCommandParser removeParser = new RemoveCommandParser(controller);
        parsers.put("uninstall", removeParser);
        parsers.put("uin", removeParser);
        parsers.put("use", new UseCommandParser(controller, settings));
        ProvisionCommandParser provisionParser = new ProvisionCommandParser(controller);
        parsers.put("pr", provisionParser);
        parsers.put("provision", provisionParser);
        ListCommandParser listCommandParser = new ListCommandParser(controller);
        parsers.put("ls", listCommandParser);
        parsers.put("list", listCommandParser);
        ProfileCommandParser profileCommandParser = new ProfileCommandParser(controller);
        parsers.put("profile", profileCommandParser);
        parsers.put("pf", profileCommandParser);
    }

    /**
     * Sets the default domain address if it is configured.
     */
    private void setDefaultAddress() {
        String defaultAddress = settings.getDomainAddress("default");
        if (defaultAddress != null) {
            controller.setDomainAddress(defaultAddress);
        }
    }

}
