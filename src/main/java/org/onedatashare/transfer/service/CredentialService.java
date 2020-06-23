package org.onedatashare.transfer.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.onedatashare.transfer.model.core.EndpointType;
import org.onedatashare.transfer.model.credential.AccountEndpointCredential;
import org.onedatashare.transfer.model.credential.OAuthEndpointCredential;
import org.onedatashare.transfer.model.error.CredentialNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Service
public class CredentialService {
    private WebClient client;

    @Value("${cred.service.url}")
    private String credentialServiceUrl;
    private static final int TIMEOUT_IN_MILLIS = 10000;

    @PostConstruct
    private void initialize(){
        TcpClient tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_IN_MILLIS)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS));
                });

        this.client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .build();
    }

    private WebClient.ResponseSpec fetchCredential(String userId, EndpointType type, String credId){
        return client.get()
                .uri(URI.create(String.format("%s/%s/%s/%s",credentialServiceUrl, userId, type, credId)))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new CredentialNotFoundException()))
                .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new Exception("Internal server error")));
    }

    public Mono<AccountEndpointCredential> fetchAccountCredential(String userId, EndpointType type, String credId){
        return fetchCredential(userId, type, credId)
                .bodyToMono(AccountEndpointCredential.class);
    }

    public Mono<OAuthEndpointCredential> fetchOAuthCredential(String userId, EndpointType type, String credId){
        return fetchCredential(userId, type, credId)
                .bodyToMono(OAuthEndpointCredential.class);
    }
}