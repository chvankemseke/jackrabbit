/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.jndi;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import javax.jcr.*;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * <code>BindableRepository</code> ...
 */
class BindableRepository implements Repository, Referenceable, Serializable {

    static final long serialVersionUID = -2298220550793843166L;

    /**
     * path to the configuration file of the repository
     */
    private final String configFilePath;
    /**
     * repository home directory
     */
    private final String repHomeDir;

    /**
     * type of <code>configFilePath</code> reference address (@see <code>{@link Reference#get(String)}</code>
     */
    static final String CONFIGFILEPATH_ADDRTYPE = "configFilePath";
    /**
     * type of <code>repHomeDir</code> reference address (@see <code>{@link Reference#get(String)}</code>
     */
    static final String REPHOMEDIR_ADDRTYPE = "repHomeDir";

    private transient Repository delegatee;

    private BindableRepository(String configFilePath, String repHomeDir) {
        this.configFilePath = configFilePath;
        this.repHomeDir = repHomeDir;
        delegatee = null;
    }

    static BindableRepository create(String configFilePath, String repHomeDir)
            throws RepositoryException {
        BindableRepository rep = new BindableRepository(configFilePath, repHomeDir);
        rep.init();
        return rep;
    }

    private void init() throws RepositoryException {
        RepositoryConfig config = RepositoryConfig.create(configFilePath, repHomeDir);
        delegatee = RepositoryImpl.create(config);
    }

    //-----------------------------------------------------------< Repository >
    /**
     * @see Repository#getProperties()
     */
    public Properties getProperties() {
        return delegatee.getProperties();
    }

    /**
     * @see Repository#getProperty(String)
     */
    public String getProperty(String s) {
        return delegatee.getProperty(s);
    }

    /**
     * @see Repository#login(Credentials, String)
     */
    public Session login(Credentials credentials, String s)
            throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return delegatee.login(credentials, s);
    }

    //--------------------------------------------------------< Referenceable >
    /**
     * @see Referenceable#getReference()
     */
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(BindableRepository.class.getName(),
                BindableRepositoryFactory.class.getName(),
                null);  // factory location
        ref.add(new StringRefAddr(CONFIGFILEPATH_ADDRTYPE, configFilePath));
        ref.add(new StringRefAddr(REPHOMEDIR_ADDRTYPE, repHomeDir));
        return ref;
    }

    //-------------------------------------------------< Serializable support >
    private void writeObject(ObjectOutputStream out) throws IOException {
        // delegate to default implementation
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // delegate deserialization to default implementation
        in.defaultReadObject();
        // initialize reconstructed instance
        try {
            init();
        } catch (RepositoryException re) {
            // failed to reinstantiate repository
            throw new IOException(re.getMessage());
        }
    }
}
