package org.onedatashare.transfer.resource;

import org.apache.commons.vfs2.*;
import org.onedatashare.transfer.model.core.EntityInfo;
import org.onedatashare.transfer.model.credential.EndpointCredential;
import org.onedatashare.transfer.model.drain.Drain;
import org.onedatashare.transfer.model.drain.VfsDrain;
import org.onedatashare.transfer.model.error.transfer.NotAFileException;
import org.onedatashare.transfer.model.tap.Tap;
import org.onedatashare.transfer.model.tap.VfsTap;

public class VfsResource extends Resource {
    protected FileSystemManager fileSystemManager;
    protected FileSystemOptions fileSystemOptions;

    VfsResource(EndpointCredential credential){
        this.credential = credential;
    }

    @Override
    public Tap getTap(EntityInfo baseInfo, EntityInfo relativeInfo) throws Exception {
        FileObject fileObject = fileSystemManager.resolveFile(baseInfo.getPath() + relativeInfo.getPath());
        if(fileObject.getType() != FileType.FILE){
            throw new NotAFileException();
        }
        FileContent content = fileObject.getContent();
        long size = content.getSize();
        return VfsTap.initialize(content.getInputStream(), size);
    }

    @Override
    public Drain getDrain(EntityInfo baseInfo, EntityInfo relativeInfo) throws Exception {
        FileObject fileObject = fileSystemManager.resolveFile(baseInfo.getPath() + relativeInfo.getPath(), fileSystemOptions);
        //Creates the required folders and file
        fileObject.createFile();
        return VfsDrain.initialize(fileObject.getContent().getOutputStream());
    }
}
