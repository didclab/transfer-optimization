package org.onedatashare.transfer.repository;

import org.onedatashare.transfer.model.TransferDetails;
import reactor.core.publisher.Mono;

public interface TransferReportRepository {
    Mono<TransferDetails> save(TransferDetails transferDetails);

    Mono<TransferDetails> findByKey(String key);
}
