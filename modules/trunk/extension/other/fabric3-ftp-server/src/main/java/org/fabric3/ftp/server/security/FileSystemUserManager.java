/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.ftp.server.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.osoa.sca.annotations.Property;

/**
 * 
 * User manager implementation that reads the credential from the file system.
 *
 * @version $Revision$ $Date$
 */
public class FileSystemUserManager implements UserManager {
    
    private Properties users = new Properties();
    
    /**
     * Logins a user using user name and password.
     * 
     * @param user Name of the user.
     * @param password Password for the user.
     * @return True if the user name and password are valid.
     */
    public boolean login(String user, String password) {
        return users.containsKey(user) && password.equals(users.getProperty(user));
    }
    
    /**
     * Login a user using X509 certificate.
     * 
     * @param certificate Certificate of the logging in user.
     * @return True if the user name and password are valid.
     */
    public boolean login(X509Certificate certificate) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Sets the name of the file that contains the users and passwords.
     * 
     * @param file Name of the file that contains the users and passwords.
     * @throws IOException If unable to load the file.
     */
    @Property
    public void setUserFile(String file) throws IOException {
        
        InputStream in = new FileInputStream(file);
        setUsers(in);
        
    }
    
    /**
     * Load the users from an input stream.
     * 
     * @param in Input stream that contains the users and passwords.
     */
    public void setUsers(InputStream in) throws IOException {
        
        try {
            users.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        
    }

}
