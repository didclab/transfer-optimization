package org.onedatashare.transfer.service;

import lombok.SneakyThrows;
import org.onedatashare.transfer.model.TransferDetails;
import org.onedatashare.transfer.model.core.*;
import org.onedatashare.transfer.model.credential.EndpointCredential;
import org.onedatashare.transfer.model.request.TransferJobRequest;
import org.onedatashare.transfer.model.request.TransferJobRequestWithMetaData;
import org.onedatashare.transfer.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.onedatashare.transfer.model.core.ODSConstants.*;
import static org.onedatashare.transfer.model.credential.CredentialConstants.*;

@Service
public class TransferService {
    @Autowired
    private CredentialService credentialService;
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    @Autowired
    private SaveToRedis saveToRedis;

    public Mono<? extends EndpointCredential> getEndpointCredential(String userId, EndpointType type, String credId) {
        if (ACCOUNT_CRED_TYPE.contains(type)) {
            return credentialService.fetchAccountCredential(userId, type, credId);
        } else if (OAUTH_CRED_TYPE.contains(type)) {
            return credentialService.fetchOAuthCredential(userId, type, credId);
        }
        return Mono.error(new Exception("Invalid endpoint type. Must either be AccountCred or OauthCred type"));
    }

    @SneakyThrows
    public Resource createResource(EndpointCredential cred, EndpointType type) {
        switch (type) {
            case http:
                return new HttpResource(cred);
            case box:
                return new BoxResource(cred);
            case ftp:
                return new FtpResource(cred);
            case dropbox:
                return new DropboxResource(cred);
            case gdrive:
                return new GDriveResource(cred);
            case sftp:
                return new SftpResource(cred);
            default:
                return null;
        }
    }

    public Mono<Void> submit(TransferJobRequestWithMetaData request) {
        logger.info("In submit Function");
        saveToRedis.save(new TransferDetails("abc", "duration"));
        logger.info("Saved in redis");

        return Mono.just(request.getOwnerId())
                .flatMap(ownerId -> {
                    logger.info("Setting credential");
                    TransferJobRequest.Source source = request.getSource();
                    TransferJobRequest.Destination destination = request.getDestination();
                    Mono<Resource> sourceResourceMono = getEndpointCredential(ownerId, source.getType(), source.getCredId())
                            .map(credential -> createResource(credential, source.getType()));
                    Mono<Resource> destinationResourceMono = getEndpointCredential(ownerId, destination.getType(), destination.getCredId())
                            .map(credential -> createResource(credential, destination.getType()));
                    return sourceResourceMono.zipWith(destinationResourceMono, Transfer::new);
                })
                .doOnNext(transfer -> {
                    logger.info("Setting EntityInfo");
                    EntityInfo es = new EntityInfo();
                    es.setId(request.getSource().getInfo().getId());
                    es.setPath(request.getSource().getInfo().getPath());
                    es.setSize(request.getSource().getInfo().getSize());

                    List<EntityInfo> ftt = new ArrayList<EntityInfo>();
                    for (TransferJobRequest.EntityInfo ei : request.getSource().getInfoList()) {
                        EntityInfo nei = new EntityInfo();
                        nei.setSize(ei.getSize());
                        nei.setPath(ei.getPath());
                        nei.setId(ei.getId());
                        ftt.add(nei);
                    }


                    EntityInfo ed = new EntityInfo();
                    ed.setId(request.getDestination().getInfo().getId());
                    ed.setPath(request.getDestination().getInfo().getPath());
                    ed.setSize(request.getDestination().getInfo().getSize());

                    transfer.setId(request.getId());
                    transfer.setSourceInfo(es);
                    transfer.setDestinationInfo(ed);
                    transfer.setFilesToTransfer(ftt);
                    logger.info("Starting Start...");
                    transfer.start(TRANSFER_SLICE_SIZE).subscribeOn(Schedulers.elastic()).subscribe();
                })
                .doOnSubscribe(s -> logger.info("Transfer submit initiated"))
                .subscribeOn(Schedulers.elastic())
                .then();
    }

    /**
     * This method cancel an ongoing transfer.
     * User email and job id passed in the request is used to obtain the job UUID,
     * which is in turn used to access the ongoing job flux from the ongoingJobs map.
     * This flux is then disposed and the job is evicted from the map to cancel the transfer.
     *
     * @param uuid
     * @return Mono of job that was stopped
     */
    public Mono<Void> cancel(UUID uuid) {
        return Mono.error(new Exception("Unsupported operation"));
    }
}
