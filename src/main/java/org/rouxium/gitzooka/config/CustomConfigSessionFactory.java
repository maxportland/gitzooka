package org.rouxium.gitzooka.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

public class CustomConfigSessionFactory extends JschConfigSessionFactory {

    private String knownHostsFile;
    private String privateKeyFile;
    private String privateKeyPassphrase;
    private String username;
    private String password;

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
        //session.setConfig("StrictHostKeyChecking", "no");
        CredentialsProvider provider = new CredentialsProvider() {
            @Override
            public boolean isInteractive() {
                return false;
            }
            @Override
            public boolean supports(CredentialItem... items) {
                return true;
            }
            @Override
            public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
                for (CredentialItem item : items) {
                    if(privateKeyPassphrase != null) {
                        ((CredentialItem.StringType) item).setValue(privateKeyPassphrase);
                    }
                }
                return true;
            }
        };
        UserInfo userInfo = new CredentialsProviderUserInfo(session, provider);
        session.setUserInfo(userInfo);
    }

    @Override
    protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException {
        JSch jsch = super.getJSch(hc, fs);
        jsch.removeAllIdentity();
        if(knownHostsFile != null) {
            jsch.setKnownHosts(knownHostsFile);
        }
        if(privateKeyFile != null) {
            jsch.addIdentity(privateKeyFile);
        }
        return jsch;
    }

    public UsernamePasswordCredentialsProvider getUsernamePasswordCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(username, password != null ? password : "");
    }

    public void setKnownHostsFile(String knownHostsFile) {
        this.knownHostsFile = knownHostsFile;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    public void setPrivateKeyPassphrase(String privateKeyPassphrase) {
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
