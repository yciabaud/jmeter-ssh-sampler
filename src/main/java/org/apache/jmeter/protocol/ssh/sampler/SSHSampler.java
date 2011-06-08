/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.jmeter.protocol.ssh.sampler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * SSH Sampler that collects single lines of output and returns
 * them as samples.
 *
 */
public class SSHSampler extends AbstractSampler implements TestBean {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private String hostname = "";
    private int port = 22;
    private String username = "";
    private String password = "";
    private String sshkeyfile = "";
    private String passphrase = "";
    private int connectionTimeout = 5000;
    private String command = "date";
    private static final JSch jsch = new JSch();
    private Session session = null;
    private String failureReason = "Unknown";
    private SSHSamplerUserInfo userinfo = null;

    public SSHSampler() {
        super();
        setName("SSH Sampler");
        userinfo = new SSHSamplerUserInfo(this);
    }

    /**
     * Returns last line of output from the command
     */
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName() + ":(" + username + "@" + hostname + ":" + port + ")");



        // Set up sampler return types
        res.setSamplerData(command);
        res.setDataType(SampleResult.TEXT);
        res.setContentType("text/plain");

        String response;
        if (session == null) {
            connect();
        }

        try {
            if (session == null) {
                log.error("Failed to connect to server with credentials "
                        + getUsername() + "@" + getHostname() + ":" + getPort()
                        + " pw=" + getPassword());
                throw new NullPointerException("Failed to connect to server: " + failureReason);
            }

            response = doCommand(session, command, res);
            res.setResponseData(response.getBytes());

            res.setSuccessful(true);
            res.setResponseCodeOK();
            res.setResponseMessageOK();
        } catch (JSchException e1) {
            res.setSuccessful(false);
            res.setResponseCode("JSchException");
            res.setResponseMessage(e1.getMessage());
        } catch (IOException e1) {
            res.setSuccessful(false);
            res.setResponseCode("IOException");
            res.setResponseMessage(e1.getMessage());
        } catch (NullPointerException e1) {
            res.setSuccessful(false);
            res.setResponseCode("Connection Failed");
            res.setResponseMessage(e1.getMessage());
        }

        // Try a disconnect/sesson = null here instead of in finalize.
        disconnect();
        session = null;
        return res;
    }

    /**
     * Executes a the given command inside a short-lived channel in the session.
     * 
     * Performance could be likely improved by reusing a single channel, though
     * the gains would be minimal compared to sharing the Session.
     *  
     * @param session Session in which to create the channel
     * @param command Command to send to the server for execution
     * @return All standard output from the command
     * @throws JSchException 
     * @throws IOException Error has occurred down in the network layer
     */
    private String doCommand(Session session, String command, SampleResult res) throws JSchException, IOException {
        StringBuilder sb = new StringBuilder();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");

        BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(channel.getErrStream()));
        channel.setCommand(command);
        res.sampleStart();
        channel.connect();

        sb.append("stdin:\n======\n\n");
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            sb.append(line);
            sb.append("\n");
        }
        sb.append("stderr:\n======\n\n");
        for (String line = err.readLine(); line != null; line = err.readLine()) {
            sb.append(line);
            sb.append("\n");
        }
        
        res.sampleEnd();

        channel.disconnect();
        return sb.toString();
    }

    /**
     * Sets up SSH Session on connection start
     */
    public void connect() {
        try {
            failureReason = "Unknown";
            session = jsch.getSession(getUsername(), getHostname(), getPort());
            // session.setPassword(getPassword()); // Use a userinfo instead
            session.setUserInfo(userinfo);
            if (userinfo.useKeyFile()) {
                jsch.addIdentity(getSshkeyfile());
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(connectionTimeout);
        } catch (JSchException e) {
            failureReason = e.getMessage();
            session.disconnect();
            session = null;
        }
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }

    // Accessors
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setSshkeyfile(String sshKeyFile) {
        this.sshkeyfile = sshKeyFile;
    }

    public String getSshkeyfile() {
        return sshkeyfile;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String server) {
        this.hostname = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public void finalize() {
        try {
            super.finalize();
        } catch (Throwable e) {
        } finally {
            if (session != null) {
                session.disconnect();
                session = null;
            }
        }
    }

    /**
     * A private implementation of com.jcraft.jsch.UserInfo. This takes a SSHSampler when constructed
     * and looks over its data when queried for information. This should only be visible to the SSH Sampler
     * class.
     */
    private class SSHSamplerUserInfo implements UserInfo {

        private SSHSampler owner;

        public SSHSamplerUserInfo(SSHSampler owner) {
            this.owner = owner;
        }

        public String getPassphrase() {
            String retval = owner.getPassphrase();
            if ((retval.length() == 0) && !useKeyFile()) {
                retval = null;
            }
            return retval;
        }

        public String getPassword() {
            String retval = owner.getPassword();
            if (retval.length() == 0) {
                retval = null;
            }
            return retval;
        }

        /* Prompts/show should be taken care of by Jmeter */
        public boolean promptPassword(String message) {
            return true;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptYesNo(String message) {
            return true;
        }

        public void showMessage(String message) {
            return;
        }

        /* 
         * These are not part of the UserInfo interface, but since this object can inspect its owner's data
         * it seems cleaner to just ask it to figure out what the user wants to do, rather than cluttering
         * the sampler code with this.
         */
        /**
         * useKeyFile returns true if owner.sshkeyfile is not empty
         */
        public boolean useKeyFile() {
            return owner.getSshkeyfile().length() > 0;
        }
    } /* Class SSHSamplerUserInfo */

}
