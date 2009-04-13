/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
