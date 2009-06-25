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
package org.fabric3.binding.ws.metro.runtime.policy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Holder;

import com.sun.xml.ws.wsdl.writer.WSDLResolver;

/**
 * Resolves WSDL and schema artifacts for a SEI to the local filesystem, typically a temporary directory. Artifacts will be marked for deletion on JVM
 * exit. Note that as deployment operations are not concurrent and the WSDL artifacts are only needed for the duration of endpoint provisioning, this
 * implementation does not need to handle naming clashes. However, it does prefix the artifact file names with the Java SEI package.
 *
 * @version $Rev$ $Date$
 */
public class WsdlFileResolver implements WSDLResolver {
    private String packageName;
    private File directory;
    private boolean client;
    private File concreteWsdl;
    private File schema;


    /**
     * Constructor.
     *
     * @param packageName the package name for SEI classes being resolved. Used to qualify the names of artifacts written to disk
     * @param directory   the directory to store the artifacts in
     * @param client      true if client WSDL is being generated
     */
    public WsdlFileResolver(String packageName, File directory, boolean client) {
        this.packageName = packageName;
        this.directory = directory;
        this.client = client;
    }

    public Result getWSDL(String fileName) {
        if (client) {
            int pos = fileName.lastIndexOf(".");
            fileName = fileName.substring(0, pos) + "Client" + fileName.substring(pos);
        }
        concreteWsdl = createFile(fileName);
        return toResult(concreteWsdl);
    }

    public Result getAbstractWSDL(Holder<String> filename) {
        return toResult(concreteWsdl);
    }

    public Result getSchemaOutput(String namespace, Holder<String> filename) {
        return getSchemaOutput(namespace, filename.value);
    }

    public File getConcreteWsdl() {
        return concreteWsdl;
    }

    public File getSchema() {
        return schema;
    }

    private File createFile(String fileName) {
        File file = new File(directory, packageName + "." + fileName);
        file.deleteOnExit();
        return file;
    }

    private Result getSchemaOutput(String namespace, String fileName) {
        if (namespace.equals("")) {
            return null;
        }
        schema = createFile(fileName);
        return toResult(schema);
    }

    private Result toResult(File file) {
        Result result;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            result = new StreamResult(fos);
            result.setSystemId(file.getPath().replace('\\', '/'));
        } catch (FileNotFoundException e) {
            throw new AssertionError(e);
        }
        return result;
    }


}
