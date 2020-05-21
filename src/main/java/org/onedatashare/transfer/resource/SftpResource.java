package org.onedatashare.transfer.resource;

import lombok.SneakyThrows;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.onedatashare.transfer.model.credential.AccountEndpointCredential;
import org.onedatashare.transfer.model.credential.EndpointCredential;

public class SftpResource extends VfsResource {
    @SneakyThrows
    public SftpResource(EndpointCredential credential) {
        super(credential);
        this.fileSystemOptions = new FileSystemOptions();
        AccountEndpointCredential accountCredential = (AccountEndpointCredential) credential;
        SftpFileSystemConfigBuilder.getInstance()
                .setPreferredAuthentications(fileSystemOptions,"password,keyboard-interactive");
        //Handling authentication
        if(accountCredential.getUsername() != null) {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(accountCredential.getAccountId(), accountCredential.getUsername(), accountCredential.getSecret());
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(this.fileSystemOptions, auth);
        }
        this.fileSystemManager = VFS.getManager();
        this.fileSystemManager.setLogger(null);
    }
}
