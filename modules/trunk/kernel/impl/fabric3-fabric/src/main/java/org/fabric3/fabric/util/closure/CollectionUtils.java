/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.util.closure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility for applying closures on collections.
 * 
 * TODO To be moved into a separate module.
 * 
 * @version $Revision$ $Date$
 */
public class CollectionUtils {
    
    private CollectionUtils() {
    }
    
    public static <OBJECT> List<OBJECT> filter(List<OBJECT> source, Closure<OBJECT, Boolean> filter) {
        
        List<OBJECT> result = new ArrayList<OBJECT>();
        
        for (OBJECT object : source) {
            if (filter.execute(object)) {
                result.add(object);
            }
        }
        
        return result;
        
    }
    
    public static <OBJECT> Set<OBJECT> filter(Set<OBJECT> source, Closure<OBJECT, Boolean> filter) {
        
        HashSet<OBJECT> result = new HashSet<OBJECT>();
        
        for (OBJECT object : source) {
            if (filter.execute(object)) {
                result.add(object);
            }
        }
        
        return result;
        
    }

}
