package org.onedatashare.transfer.repository;

import org.onedatashare.transfer.model.TransferDetails;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class RedisTransferReportRepository implements TransferReportRepository {

    private final ReactiveRedisOperations<String, String> operations;

    public RedisTransferReportRepository(@Qualifier("reactiveRedisOperations") ReactiveRedisOperations<String, String> operations) {
        this.operations = operations;
    }

    @Override
    public Mono<TransferDetails> save(TransferDetails transferDetails) {
        return operations.opsForValue()
                .set(transferDetails.getFileName(), transferDetails.getDuration() + "")
                .map(__ -> transferDetails);
    }

    @Override
    public Mono<TransferDetails> findByKey(String key) {
        return operations.opsForValue()
                .get(key)
                .map(result -> new TransferDetails(result, Long.parseLong(key)));
    }
}
