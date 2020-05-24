package org.onedatashare.transfer.service;

import org.onedatashare.transfer.model.TransferDetails;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class SaveToRedis {

    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, TransferDetails> redisOperations;

    public SaveToRedis(ReactiveRedisConnectionFactory factory, ReactiveRedisOperations<String, TransferDetails> redisOperations) {
        this.factory = factory;
        this.redisOperations = redisOperations;
    }

    public void save(TransferDetails transferDetails){
        factory.getReactiveConnection().serverCommands().flushAll().thenMany(
                Flux.just(transferDetails.getFileName())
                        .map(name -> new TransferDetails(name, "duration"))
                        .flatMap(td -> redisOperations.opsForValue().set(td.getFileName(), td)))
                .thenMany(redisOperations.keys("*")
                        .flatMap(redisOperations.opsForValue()::get))
                .subscribe(System.out::println);
    }

}
