package org.onedatashare.transfer.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.onedatashare.transfer.model.error.CredentialNotFoundException;
import org.onedatashare.transfer.model.request.TransferJobRequest;
import org.onedatashare.transfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Contoller for handling file/folder transfer requests
 */
@RestController
@RequestMapping("/api/transfer")
public class TransferController {
    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    @Autowired
    private TransferService transferService;

    /**
     * Handler for POST requests of transfers
     * @param request - Request data with transfer information
     * @return Mono\<Job\>
     */
    @PostMapping
    public Mono<Void> start(@RequestBody TransferJobRequest request) {
        return transferService.submit(request);
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<String> handle(CredentialNotFoundException nfException) {
        logger.error(nfException.toString());
        return new ResponseEntity<>(nfException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}