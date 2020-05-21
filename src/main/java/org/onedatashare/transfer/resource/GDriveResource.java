package org.onedatashare.transfer.resource;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.services.drive.Drive;
import org.onedatashare.transfer.config.GDriveConfig;
import org.onedatashare.transfer.model.core.EntityInfo;
import org.onedatashare.transfer.model.credential.EndpointCredential;
import org.onedatashare.transfer.model.credential.OAuthEndpointCredential;
import org.onedatashare.transfer.model.drain.Drain;
import org.onedatashare.transfer.model.drain.GDriveDrain;
import org.onedatashare.transfer.model.tap.GDriveTap;
import org.onedatashare.transfer.model.tap.Tap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.onedatashare.transfer.model.core.ODSConstants.DRIVE_URI_SCHEME;

public final class GDriveResource extends Resource {
    public static GDriveConfig gDriveConfig = new GDriveConfig();
    private static final String DOWNLOAD_URL = "https://www.googleapis.com/drive/v3/files/{}?alt=media";
    private Drive driveService;

    public GDriveResource(EndpointCredential cred) throws IOException {
        super(cred);
        this.driveService = gDriveConfig.getDriveService((OAuthEndpointCredential) cred);
    }

    @Override
    public Tap getTap(EntityInfo baseInfo, EntityInfo relativeInfo) throws Exception {
        String downloadUrl = DOWNLOAD_URL.replace("{}", relativeInfo.getId());
        HttpRequest httpRequestGet = driveService.getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl));
        return GDriveTap.initialize(httpRequestGet, relativeInfo.getSize());
    }

    @Override
    public Drain getDrain(EntityInfo baseInfo, EntityInfo relativeInfo) throws Exception {
        String name = relativeInfo.getPath();
        int pos = name.lastIndexOf("/");
        if(pos != -1) {
            name = name.substring(pos);
        }
        return GDriveDrain.initialize(((OAuthEndpointCredential) this.credential).getToken(), name , baseInfo.getId());
    }

    @Override
    public String pathFromUri(String uri) throws UnsupportedEncodingException {
        String path = "";
        if(uri.contains(DRIVE_URI_SCHEME)){
            path = uri.substring(DRIVE_URI_SCHEME.length() - 1);
        }
        path = java.net.URLDecoder.decode(path, "UTF-8");
        return path;
    }

}
