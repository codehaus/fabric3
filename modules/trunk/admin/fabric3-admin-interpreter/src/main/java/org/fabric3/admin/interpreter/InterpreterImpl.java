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
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.cli.DomainAdminLexer;
import org.fabric3.admin.cli.DomainAdminParser;
import org.fabric3.admin.interpreter.parser.AuthCommandParser;
import org.fabric3.admin.interpreter.parser.DeployCommandParser;
import org.fabric3.admin.interpreter.parser.InstallCommandParser;
import org.fabric3.admin.interpreter.parser.ListCommandParser;
import org.fabric3.admin.interpreter.parser.RemoveCommandParser;
import org.fabric3.admin.interpreter.parser.UndeployCommandParser;
import org.fabric3.admin.interpreter.parser.UninstallCommandParser;

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
    private DomainController controller;
    private Map<Integer, CommandParser> parsers;

    public InterpreterImpl(DomainController controller) {
        this.controller = controller;
        createParsers();
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
        // Run the lexer and token parser on the line.
        DomainAdminLexer lexer = new DomainAdminLexer(new ANTLRStringStream(line));
        DomainAdminParser parser = new DomainAdminParser(new CommonTokenStream(lexer));

        DomainAdminParser.command_return ret;
        try {
            ret = parser.command();
        } catch (RecognitionException e) {
            // TODO interpret the exception
            throw new InterpreterException(e);
        }

        // construct the AST and walk it
        CommonTree tree = (CommonTree) ret.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        Iterator<Token> iterator = new CommonTreeIterator(nodes.iterator());

        Command command = parseCommand(iterator);

        try {
            command.execute(out);
        } catch (CommandException e) {
            out.println("ERORR: An error was encountered");
            e.printStackTrace(out);
        }
    }

    /**
     * Parses the command in the AST and returns a corresponding Command object.
     *
     * @param iterator the AST iterator
     * @return the command object
     * @throws ParseException if an exception parsing the AST occurs
     */
    private Command parseCommand(Iterator<Token> iterator) throws ParseException {
        // advance to the command token
        Token commandToken = iterator.next();
        CommandParser cmdParser = parsers.get(commandToken.getType());
        if (cmdParser == null) {
            // this would represent an error in the grammar as an unrecognixzed command should throw an error when the parse tree is constructed
            throw new AssertionError("Command not recognized: " + commandToken.getText());
        }
        // advance past the DOWN token after the command token
        iterator.next();
        return cmdParser.parse(iterator);
    }

    private void createParsers() {
        parsers = new HashMap<Integer, CommandParser>();
        parsers.put(DomainAdminLexer.INSTALL_CMD, new InstallCommandParser(controller));
        parsers.put(DomainAdminLexer.AUTH_CMD, new AuthCommandParser(controller));
        parsers.put(DomainAdminLexer.LIST_CMD, new ListCommandParser(controller));
        parsers.put(DomainAdminLexer.DEPLOY_CMD, new DeployCommandParser(controller));
        parsers.put(DomainAdminLexer.UNDEPLOY_CMD, new UndeployCommandParser(controller));
        parsers.put(DomainAdminLexer.UNINSTALL_CMD, new UninstallCommandParser(controller));
        parsers.put(DomainAdminLexer.REMOVE_CMD, new RemoveCommandParser(controller));
    }

    /**
     * Dumps the tokens in the AST. Used for testing.
     *
     * @param iterator the token iterator to walk
     */
    private void dumpTokens(Iterator<Token> iterator) {
        while (iterator.hasNext()) {
            Object o = iterator.next();
            System.out.println("--->" + o + ":" + o.getClass());
            o.toString();
        }
    }
}
