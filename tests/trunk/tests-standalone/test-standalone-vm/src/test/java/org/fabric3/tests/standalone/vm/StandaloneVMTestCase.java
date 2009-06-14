/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.tests.standalone.vm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.impl.DomainControllerImpl;
import org.fabric3.management.contribution.ContributionInfo;

/**
 * Runs basic smoketests for the standalone runtime booted in single-VM mode.
 *
 * @version $Revision$ $Date$
 */
public class StandaloneVMTestCase {
    private static final File RUNTIME_DIR = new File(".." + File.separator
            + "test-standalone-setup" + File.separator
            + "target" + File.separator
            + "assembly" + File.separator + "bin");

    private static final File APP_DIR = new File(".." + File.separator
            + "test-standalone-app" + File.separator
            + "target" + File.separator
            + "test-standalone-app-0.1-SNAPSHOT.jar");

    @BeforeClass
    public static void bootServer() throws Exception {
        Process process = Runtime.getRuntime().exec("java -jar server.jar", new String[0], RUNTIME_DIR);
        InputStream stream = process.getErrorStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while (stream.available() == 0) {
            Thread.sleep(100);
        }
        byte[] b = new byte[stream.available()];
        stream.read(b);
        os.write(b);
        String output = new String(os.toByteArray());
        System.out.println(output);
        Assert.assertFalse(output.indexOf("SEVERE") >= 0);
    }

    @Test
    public void testDeployUndeploy() throws Exception {
        Exception exception = null;
        DomainController domain = new DomainControllerImpl();
        for (int i = 0; i < 50; i++) {   // wait 5 seconds
            Thread.sleep(100);
            try {
                domain.connect();
                Set<ContributionInfo> infos = domain.stat();
                Assert.assertFalse(infos.isEmpty());
                exception = null;
                break;
            } catch (Exception e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
        URL url = APP_DIR.toURI().toURL();
        URI uri = URI.create("test-standalone-app-0.1-SNAPSHOT.jar");
        // deploy and undeploy twice
        for (int i = 0; i < 2; i++) {   // wait 5 seconds
            domain.store(url, uri);
            domain.install(uri);
            domain.deploy(uri);
            domain.undeploy(uri);
            domain.uninstall(uri);
            domain.remove(uri);
        }
        domain.disconnect();
    }

    @AfterClass
    public static void shutdownServer() throws Exception {
        Process shutdown = Runtime.getRuntime().exec("java -jar shutdown.jar", new String[0], RUNTIME_DIR);
        shutdown.waitFor();
    }
}
