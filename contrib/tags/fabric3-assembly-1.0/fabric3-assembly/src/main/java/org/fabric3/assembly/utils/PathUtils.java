package org.fabric3.assembly.utils;

import java.io.File;

/**
 * @author Michal Capo
 */
public class PathUtils {

    public static String MAVEN_FOLDER;

    public static String TMP_FOLDER;

    static {
        String userHome = System.getProperty("user.home");
        String m2 = ".m2" + File.separator + "repository";

        MAVEN_FOLDER = userHome + "/" + m2 + "/";
        TMP_FOLDER = System.getProperty("java.io.tmpdir");
    }

}
