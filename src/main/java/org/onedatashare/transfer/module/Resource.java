package org.onedatashare.transfer.module;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs2.FileSystemException;
import org.onedatashare.transfer.model.core.IdMap;
import org.onedatashare.transfer.model.credential.EndpointCredential;
import org.onedatashare.transfer.model.drain.Drain;
import org.onedatashare.transfer.model.tap.Tap;

import java.io.UnsupportedEncodingException;

@NoArgsConstructor
public class Resource {
    EndpointCredential credential;

    Resource(EndpointCredential credential){
        this.credential = credential;
    }

    public Tap getTap(IdMap idMap){
        throw new NotImplementedException();
    }

    public Drain getDrain(IdMap idMap){
        throw new NotImplementedException();
    }

    public String pathFromUri(String uri) throws UnsupportedEncodingException {
        String path = "";
        path = java.net.URLDecoder.decode(path, "UTF-8");
        return path;
    }

}
