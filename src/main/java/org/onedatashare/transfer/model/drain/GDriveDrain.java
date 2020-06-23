package org.onedatashare.transfer.model.drain;

import org.onedatashare.transfer.model.core.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class GDriveDrain implements Drain {
    private ByteArrayOutputStream chunk = new ByteArrayOutputStream();
    private long size = 0;
    private String resumableSessionURL;
    private String parentId;

    private static final Logger logger = LoggerFactory.getLogger(GDriveDrain.class);

    private static final String UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable";

    private GDriveDrain(){}

    public static GDriveDrain initialize(String token, String name, String parentId) throws Exception{
        GDriveDrain driveDrain = new GDriveDrain();
        try{
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod(RequestMethod.POST.name());
            request.setDoInput(true);
            request.setDoOutput(true);
            request.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            request.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
            String body;
            if(parentId !=null){
                body = "{\"name\": \"" + name + "\", \"parents\": [\"" + parentId + "\"]}";
            }else{
                body = "{\"name\": \"" + name + "\"}";
            }
            request.setRequestProperty(HttpHeaders.CONTENT_LENGTH, String.format(Locale.ENGLISH, "%d", body.getBytes().length));

            OutputStream outputStream = request.getOutputStream();
            outputStream.write(body.getBytes());
            outputStream.close();
            request.connect();
            int uploadRequestResponseCode  = request.getResponseCode();
            if(uploadRequestResponseCode == HttpURLConnection.HTTP_OK) {
                driveDrain.resumableSessionURL = request.getHeaderField("location");
            }else{
                throw new Exception("Transfer will fail");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return driveDrain;
    }

    @Override
    public void drain(Slice slice) throws Exception{
        chunk.write(slice.asBytes());

        // Google drive only supports 258KB (1 << 18) of data transfer per request
        int chunks = chunk.size() / (1<<18);
        int sizeUploading = chunks * (1<<18);

        URL url = new URL(resumableSessionURL);
        if(sizeUploading > 0) {
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod(RequestMethod.PUT.name());
            request.setConnectTimeout(10000);
            request.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Long.toString(sizeUploading));
            request.setRequestProperty(HttpHeaders.CONTENT_RANGE,
                    String.format("bytes %d-%d/*", size ,(size + sizeUploading - 1)));
            request.setDoOutput(true);
            OutputStream outputStream = request.getOutputStream();
            outputStream.write(chunk.toByteArray(), 0, sizeUploading);
            outputStream.close();
            request.connect();

            if (request.getResponseCode() == 308) {
                size = size + sizeUploading;
                ByteArrayOutputStream temp = new ByteArrayOutputStream();
                temp.write(chunk.toByteArray(), sizeUploading, (chunk.size() - sizeUploading));
                chunk = temp;
            } else if (request.getResponseCode() == HttpURLConnection.HTTP_OK || request.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                logger.debug("code: " + request.getResponseCode() +
                        ", message: " + request.getResponseMessage());
            } else {
                logger.debug("code: " + request.getResponseCode() +
                        ", message: " + request.getResponseMessage());
            }
        }
    }

    @Override
    public void finish() {
        try{
            URL url = new URL(resumableSessionURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("PUT");
            request.setConnectTimeout(10000);
            request.setRequestProperty("Content-Length", Long.toString(chunk.size()));
            if(chunk.size() == 0)
                request.setRequestProperty("Content-Range", "bytes */" + (size+chunk.size()));
            else
                request.setRequestProperty("Content-Range", "bytes " + size + "-" + (size+chunk.size()-1) + "/" + (size + chunk.size()));
            request.setDoOutput(true);
            OutputStream outputStream = request.getOutputStream();
            outputStream.write(chunk.toByteArray(), 0, chunk.size());
            outputStream.close();
            request.connect();
            if(request.getResponseCode() == 200 || request.getResponseCode() == 201){

            }else {

            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

