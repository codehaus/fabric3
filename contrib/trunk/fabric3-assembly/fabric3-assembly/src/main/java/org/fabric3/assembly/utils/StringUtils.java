package org.fabric3.assembly.utils;

/**
 * @author Michal Capo
 */
public class StringUtils {

    public static boolean isBlank(String pString) {
        return null == pString || 0 == pString.trim().length();
    }

}
