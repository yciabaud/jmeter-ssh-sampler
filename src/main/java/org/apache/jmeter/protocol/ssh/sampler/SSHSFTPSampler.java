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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * SSH Sampler that collects single lines of output and returns
 * them as samples.
 *
 */
public class SSHSFTPSampler extends AbstractSSHSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final String SFTP_COMMAND_GET = "get";
    public static final String SFTP_COMMAND_PUT = "put";
    public static final String SFTP_COMMAND_RM = "rm";
    public static final String SFTP_COMMAND_RMDIR = "rmdir";
    public static final String SFTP_COMMAND_MKDIR = "mkdir";
    public static final String SFTP_COMMAND_LS = "ls";
    public static final String SFTP_COMMAND_RENAME = "rename";
    private String source;
    private String destination;
    private String action;
    private boolean printFile = true;

    public SSHSFTPSampler() {
        super("SSH SFTP Sampler");
    }

    /**
     * Returns last line of output from the command
     */
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName() + ":(" + getUsername() + "@" + getHostname() + ":" + getPort() + ")");



        // Set up sampler return types
        res.setSamplerData(action + " " + source);

        res.setDataType(SampleResult.TEXT);
        res.setContentType("text/plain");

        String response;
        if (getSession() == null) {
            connect();
        }

        try {
            if (getSession() == null) {
                log.error("Failed to connect to server with credentials "
                        + getUsername() + "@" + getHostname() + ":" + getPort()
                        + " pw=" + getPassword());
                throw new NullPointerException("Failed to connect to server: " + getFailureReason());
            }

            response = doFileTransfer(getSession(), source, destination, res);
            res.setResponseData(response.getBytes());


            res.setSuccessful(true);

            res.setResponseMessageOK();
        } catch (JSchException e1) {
            res.setSuccessful(false);
            res.setResponseCode("JSchException");
            res.setResponseMessage(e1.getMessage());
        } catch (SftpException e1) {
            res.setSuccessful(false);
            res.setResponseCode("SftpException");
            res.setResponseMessage(e1.getMessage());
        } catch (IOException e1) {
            res.setSuccessful(false);
            res.setResponseCode("IOException");
            res.setResponseMessage(e1.getMessage());
        } catch (NullPointerException e1) {
            res.setSuccessful(false);
            res.setResponseCode("Connection Failed");
            res.setResponseMessage(e1.getMessage());
        } finally {
            // Try a disconnect/sesson = null here instead of in finalize.
            disconnect();
            setSession(null);
        }
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
     * @throws SftpException
     * @throws IOException
     */
    private String doFileTransfer(Session session, String src, String dst, SampleResult res) throws JSchException, SftpException, IOException {
        StringBuilder sb = new StringBuilder();
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");

        res.sampleStart();
        channel.connect();

        if (SFTP_COMMAND_GET.equals(action)) {

            if (!printFile) {
                channel.get(src, dst);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(channel.get(src)));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    sb.append(line);
                    sb.append("\n");
                }
            }

        } else if (SFTP_COMMAND_PUT.equals(action)) {
            channel.put(src, dst);
        } else if (SFTP_COMMAND_LS.equals(action)) {
            List<ChannelSftp.LsEntry> ls = channel.ls(src);
            for (ChannelSftp.LsEntry line : ls) {
                sb.append(line.getLongname());
                sb.append("\n");
            }
        } else if (SFTP_COMMAND_RM.equals(action)) {
            channel.rm(src);
        } else if (SFTP_COMMAND_RMDIR.equals(action)) {
            channel.rmdir(src);
        } else if (SFTP_COMMAND_MKDIR.equals(action)) {
        	channel.mkdir(src);
        } else if (SFTP_COMMAND_RENAME.equals(action)) {
            channel.rename(src, dst);
        }

        res.sampleEnd();


        channel.disconnect();
        return sb.toString();
    }

    // Accessors
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean getPrintFile() {
        return printFile;
    }

    public void setPrintFile(boolean printFile) {
        this.printFile = printFile;
    }
}
