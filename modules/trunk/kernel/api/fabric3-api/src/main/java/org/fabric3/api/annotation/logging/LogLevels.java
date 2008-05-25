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

package org.fabric3.api.annotation.logging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
* Defines logging levels recognised by the {@link LogLevel} annotation.
* The log levels supported by the logging implementation underlying any given
* monitor factory implementation may not match the levels defined here and so
* monitor factories may be required to carry out a mapping between levels 
**/  
public enum LogLevels {

    SEVERE,
    
    WARNING,
    
    INFO,
    
    CONFIG,
    
    FINE,
    
    FINER,
    
    FINEST;
    
    /**
     * Encapsulates the logic used to read monitor method log level annotations. 
     * Argument <code>Method</code> instances should be annotated with a {@link LogLevel} directly
     * or with one of the level annotations which have a {@link LogLevel} meta-annotation.  
     * @param the annotated monitor method  
     * @return the <code>LogLevels</code> value defined by a direct {@link LogLevel} annotation 
     */
    public static LogLevels getAnnotatedLogLevel(Method method) {
        LogLevels level = null;
        
        LogLevel annotation = method.getAnnotation(LogLevel.class);
        if (annotation != null) {
            level = annotation.value();
        }
        
        if (level == null) {
            for (Annotation methodAnnotation : method.getDeclaredAnnotations()) {
                Class<? extends Annotation> annotationType = methodAnnotation.annotationType();
                
                LogLevel logLevel = annotationType.getAnnotation(LogLevel.class);
                if (logLevel != null) {
                    level = logLevel.value();
                    break;
                }
            }            
        }
        
        return level;
    }  
    
}
