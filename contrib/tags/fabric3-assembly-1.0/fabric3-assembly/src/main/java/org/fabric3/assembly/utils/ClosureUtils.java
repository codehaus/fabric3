package org.fabric3.assembly.utils;

import java.util.Collection;

/**
 * Some helping method for easier work with closures.
 *
 * @author Michal Capo
 */
public class ClosureUtils {

    public static <T> void each(Collection<T> pCollection, Closure<T> pClosure) {
        for (T t : pCollection) {
            pClosure.exec(t);
        }
    }

}
