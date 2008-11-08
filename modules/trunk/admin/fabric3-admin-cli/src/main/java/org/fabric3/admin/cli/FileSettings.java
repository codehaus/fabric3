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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fabric3.admin.interpreter.Settings;

/**
 * An implementation that stores settings to a properties file.
 *
 * @version $Revision$ $Date$
 */
public class FileSettings implements Settings {
    private File file;
    private Properties domains = new Properties();

    public FileSettings(File file) {
        this.file = file;
    }

    public void addDomain(String name, String address) {
        domains.put(name, address);
    }

    public String getDomainAddress(String name) {
        return (String) domains.get(name);
    }

    public Map<String, String> getDomainAddresses() {
        Map<String, String> addresses = new HashMap<String, String>(domains.size());
        for (Map.Entry<Object, Object> entry : domains.entrySet()) {
            addresses.put((String) entry.getKey(), (String) entry.getValue());
        }
        return addresses;
    }

    public void save() throws IOException {
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            domains.store(stream, "F3 domain configuration");
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }
        InputStream stream = null;
        try {
            domains.clear();
            stream = new FileInputStream(file);
            domains.load(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
