package org.rouxium.gitzooka.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomConfigSessionFactory extends JschConfigSessionFactory {

    @Value("${ssh.known_hosts_file}")
    private String knownHostsFile;

    @Value("${ssh.private_key_file}")
    private String privateKeyFile;

    @Value("${ssh.private_key_passphrase}")
    private String privateKeyPassphrase;

    @Value("${ssh.username}")
    private String username;

    @Value("${ssh.password}")
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
                    ((CredentialItem.StringType) item).setValue(privateKeyPassphrase);
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
        jsch.setKnownHosts(knownHostsFile);
        jsch.addIdentity(privateKeyFile);
        return jsch;
    }

    public UsernamePasswordCredentialsProvider getUsernamePasswordCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(username,password);
    }

}
