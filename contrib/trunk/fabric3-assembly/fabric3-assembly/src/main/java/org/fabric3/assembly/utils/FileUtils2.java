package org.fabric3.assembly.utils;

import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.exception.AssemblyException;

import java.io.File;
import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class FileUtils2 {

    public static boolean recreateFolderIfNeeded(final File pFolder, final UpdatePolicy pPolicy) {
        if (UpdatePolicyUtils.shouldUpdate(pFolder, pPolicy)) {
            FileUtils.delete(pFolder);

            if (!pFolder.mkdirs()) {
                throw new AssemblyException(MessageFormat.format("Cannot create folder: {0}", pFolder.getAbsolutePath()));
            }

            return true;
        }

        return false;
    }

    public static EFileExtension fileExtension(final File pFile) {
        if (null == pFile || !pFile.exists()) {
            return EFileExtension.UNKNOWN;
        }

        if (pFile.getName().endsWith(".jar")) {
            return EFileExtension.JAR;
        }

        if (pFile.getName().endsWith(".zip")) {
            return EFileExtension.ZIP;
        }

        return EFileExtension.UNKNOWN;
    }

}
