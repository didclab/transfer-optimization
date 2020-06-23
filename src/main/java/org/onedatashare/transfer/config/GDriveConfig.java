package org.onedatashare.transfer.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import lombok.Data;
import org.onedatashare.transfer.model.credential.OAuthEndpointCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

@Data
public class GDriveConfig {
    private static final Logger logger = LoggerFactory.getLogger(GDriveConfig.class);

    private String appName;
    private String authUri;
    private String tokenUri;
    private String authProviderX509CertUrl;
    private String redirectUri;
    private String clientId;
    private String clientSecret;
    private String projectId;

    private GoogleClientSecrets clientSecrets;
    private GoogleAuthorizationCodeFlow flow;


    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private HttpTransport httpTransport;

    public final static List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE);
    public final static String ACCESS_TYPE = "offline";
    public final static String APPROVAL_PROMPT = "force";

    private static String getValueFromResourceString(String str){
        if(str == null){
            return null;
        }
        //Env variable
        else if(str.startsWith("$")){
            if(str.endsWith("}")) {
                str = str.substring(2, str.length() - 1);
                return System.getenv(str);
            }
            else {
                ResourceBundle resource = ResourceBundle.getBundle("application");
                StringBuilder stringBuilder = new StringBuilder();
                for (String s: str.split("}")){
                    System.out.println("s");
                }
            }
        }
        //Just value
        else{
            return str;
        }
        return null;
    }

    public GDriveConfig(){
        ResourceBundle resource = ResourceBundle.getBundle("application");
        this.appName = getValueFromResourceString(resource.getString("gdrive.appName"));
        this.authUri = getValueFromResourceString(resource.getString("gdrive.authUri"));
        this.tokenUri = getValueFromResourceString(resource.getString("gdrive.tokenUri"));
        this.authProviderX509CertUrl = getValueFromResourceString(resource.getString("gdrive.authUri"));
        this.redirectUri = getValueFromResourceString(resource.getString("redirect.uri.string")) + "/api/oauth/gdrive";
        this.clientId = getValueFromResourceString(resource.getString("gdrive.clientId"));
        this.clientSecret = getValueFromResourceString(resource.getString("gdrive.clientSecret"));
        this.projectId = getValueFromResourceString(resource.getString("gdrive.projectId"));

        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                .setAuthUri(authUri)
                .setRedirectUris(Arrays.asList(redirectUri))
                .setTokenUri(tokenUri)
                .setClientId(clientId)
                .setClientSecret(clientSecret);

        clientSecrets = new GoogleClientSecrets()
                .setInstalled(details);

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, clientSecrets, SCOPES)
                    .setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
                    .setAccessType(ACCESS_TYPE)
                    .setApprovalPrompt(APPROVAL_PROMPT)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) {
                try {
                    requestInitializer.initialize(httpRequest);
                    httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
                    httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
                } catch (IOException ioe) {
                    logger.error("IOException occurred in GoogleDriveSession.setHttpTimeout()", ioe);
                } catch (NullPointerException npe) {
                    logger.error("IOException occurred in GoogleDriveSession.setHttpTimeout()", npe);
                }
            }
        };
    }

    public Drive getDriveService(OAuthEndpointCredential credential) throws IOException {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(credential.getToken());
        tokenResponse.setRefreshToken(credential.getRefreshToken());
        tokenResponse.setFactory(JacksonFactory.getDefaultInstance());
        Credential cred = this.getFlow().createAndStoreCredential(tokenResponse, String.valueOf(UUID.randomUUID()));
        return new Drive.Builder(
                this.getHttpTransport(), this.getJsonFactory(), setHttpTimeout(cred))
                .setApplicationName(this.getAppName())
                .build();
    }

}
