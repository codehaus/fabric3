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
package org.fabric3.admin.cli;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.impl.DomainControllerImpl;
import org.fabric3.admin.interpreter.Interpreter;
import org.fabric3.admin.interpreter.InterpreterException;
import org.fabric3.admin.interpreter.InterpreterImpl;

/**
 * Main entry point for the domain administation command line tool.
 *
 * @version $Revision$ $Date$
 */
public class Main {

    /**
     * Executes either a single instruction passed from the command line or enters into interactive mode.
     *
     * @param args the instruction to execture or an empty array
     * @throws InterpreterException if an error occurs executing an instruction or set of instructions
     */
    public static void main(String[] args) throws InterpreterException {
        DomainController controller = new DomainControllerImpl();
        FileSettings settings = new FileSettings(getSettingsFile());
        try {
            settings.load();
            if (settings.getDomainAddress("default") == null) {
                settings.addDomain("default", "service:jmx:rmi:///jndi/rmi://localhost:1099/server");
            }
        } catch (IOException e) {
            throw new InterpreterException("Error loading settings", e);
        }
        Interpreter interpreter = new InterpreterImpl(controller, settings);
        if (args.length == 0) {
            System.out.println("\nFabric3 Admininstration Interface");
            interpreter.processInteractive(System.in, System.out);
        } else {
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(" ").append(arg);
            }
            interpreter.process(builder.toString(), System.out);
        }
    }

    /**
     * Returns the location of the settings.properties file by introspecting the location of the current class. It is assumed the settings file is
     * contained in a sibling directory named "config".
     *
     * @return the location of the settings file
     * @throws IllegalStateException if the class cannot be introspected
     */
    private static File getSettingsFile() throws IllegalStateException {
        Class<?> clazz = Main.class;
        String name = clazz.getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";

        // get location of the class
        URL url = clazz.getResource(name);
        if (url == null) {
            throw new IllegalStateException("Unable to get location of class " + name);
        }

        String jarLocation = url.toString();
        if (!jarLocation.startsWith("jar:")) {
            throw new IllegalStateException("Must be run from a jar: " + url);
        }

        // extract the location of thr jar from the resource URL
        jarLocation = jarLocation.substring(4, jarLocation.lastIndexOf("!/"));
        if (!jarLocation.startsWith("file:")) {
            throw new IllegalStateException("Must be run from a local filesystem: " + jarLocation);
        }

        File jarFile = new File(URI.create(jarLocation));
        File configDir = new File(jarFile.getParentFile().getParentFile(), "config");
        return new File(configDir, "settings.properties");
    }


}
