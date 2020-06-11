package org.onedatashare.transfer.resource;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3Object;
import org.onedatashare.transfer.model.core.EntityInfo;
import org.onedatashare.transfer.model.core.ODSConstants;
import org.onedatashare.transfer.model.credential.AccountEndpointCredential;
import org.onedatashare.transfer.model.credential.EndpointCredential;
import org.onedatashare.transfer.model.drain.Drain;
import org.onedatashare.transfer.model.drain.S3Drain;
import org.onedatashare.transfer.model.tap.S3Tap;
import org.onedatashare.transfer.model.tap.Tap;

import java.io.UnsupportedEncodingException;

public class S3Resource extends Resource {
    private AmazonS3Client s3Client;
    public S3Resource(EndpointCredential credential) {
        super(credential);
        AccountEndpointCredential accountCredential = (AccountEndpointCredential) credential;
        AWSCredentials creds = new BasicAWSCredentials(accountCredential.getUsername(), accountCredential.getSecret());
        this.s3Client = new AmazonS3Client(creds);

    }

    @Override
    public Tap getTap(EntityInfo baseInfo, EntityInfo relativeInfo){
        try {
            String url = this.pathFromUri(baseInfo.getPath() + relativeInfo.getPath());
            url = url.replaceFirst("amazons3/", "");
            String bucketName = url.split("/")[0];
            String key = url.substring(url.indexOf('/')).replaceFirst("/", ""); //Not pretty, but the "filename" (key) needs to include the full path minus the bucketname
            String fileName = url.substring((url.lastIndexOf("/"))).replaceFirst("/", "");
            ResponseHeaderOverrides headerOverrides = new ResponseHeaderOverrides()
                    .withCacheControl("No-cache")
                    .withContentDisposition("attachment; filename=" + fileName);
            GetObjectRequest getObjectRequestHeaderOverride = new GetObjectRequest(bucketName, key)
                    .withResponseHeaders(headerOverrides);
            S3Object headerOverrideObject = s3Client.getObject(getObjectRequestHeaderOverride);
            long size = headerOverrideObject.getObjectMetadata().getContentLength();
            return S3Tap.initialize(headerOverrideObject, size);
        } catch(AmazonServiceException e){
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Drain getDrain(EntityInfo baseInfo, EntityInfo relativeInfo) throws Exception {
        String url = this.pathFromUri(baseInfo.getPath() + relativeInfo.getPath());
        url = url.replaceFirst("amazons3/", "");
        String bucketName = url.split("/")[0];
        String keyName = url.substring(url.indexOf('/')).replaceFirst("/", ""); //Not pretty, but the "filename" (key) needs to include the full path minus the bucketname
        return S3Drain.getInstance(s3Client, bucketName, keyName);
    }
    @Override
    public String pathFromUri(String uri) {
        String path = uri.replace(ODSConstants.AMAZONS3_URI_SCHEME, "amazons3/");
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }
}
