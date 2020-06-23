package org.onedatashare.transfer.service;

import lombok.SneakyThrows;
import org.onedatashare.transfer.model.TransferDetails;
import org.onedatashare.transfer.model.core.*;
import org.onedatashare.transfer.model.credential.EndpointCredential;
import org.onedatashare.transfer.model.request.TransferJobRequest;
import org.onedatashare.transfer.repository.TransferReportRepository;
import org.onedatashare.transfer.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

import static org.onedatashare.transfer.model.core.ODSConstants.*;
import static org.onedatashare.transfer.model.credential.CredentialConstants.*;

@Service
public class TransferService {
    @Autowired
    private CredentialService credentialService;

    private TransferReportRepository transferReportRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

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
            case s3:
                return new S3Resource(cred);
            default:
                return null;
        }
    }

    private Mono<String> getUserCredFromRequest() {
        return ReactiveSecurityContextHolder.getContext()
                .map(s -> {
                    Authentication authentication = s.getAuthentication();
                    return (String) authentication.getCredentials();
                });
    }


    public Mono<Void> submit(TransferJobRequest request) {
        logger.info("In submit Function");
//        transferDetailsRepository.saveAll(Flux.just(new TransferDetails(Transfer.fName,12l))).subscribe();
        return Mono.just(request)
                .flatMap(req -> {
                    TransferJobRequest.Source source = request.getSource();
                    TransferJobRequest.Destination destination = request.getDestination();
                    Mono<Resource> sourceResourceMono = getEndpointCredential(request.getOwnerId(), source.getType(), source.getCredId())
                            .map(credential -> createResource(credential, source.getType()));
                    Mono<Resource> destinationResourceMono = getEndpointCredential(request.getOwnerId(), destination.getType(), destination.getCredId())
                            .map(credential -> createResource(credential, destination.getType()));
                    return sourceResourceMono.zipWith(destinationResourceMono, Transfer::new);
                })
                .doOnNext(transfer -> {
                    transfer.setId(request.getId());
                    transfer.setSourceInfo(request.getSource().getInfo());
                    transfer.setDestinationInfo(request.getDestination().getInfo());
                    transfer.setFilesToTransfer(request.getSource().getInfoList());
                    transfer.start(TRANSFER_SLICE_SIZE).subscribeOn(Schedulers.elastic()).subscribe();
                })
                .doOnSubscribe(s -> logger.info("Transfer submit initiated"))
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
