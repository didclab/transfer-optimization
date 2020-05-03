package org.onedatashare.transfer.repository;

import org.onedatashare.transfer.model.core.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface JobRepository extends ReactiveMongoRepository<Job, UUID> {

    String fieldFilter = "{'status' : 1, 'bytes' : 1, 'job_id' : 1, 'owner' : 1, 'times' : 1, 'src.uri' : 1, 'dest.uri' : 1}";

    @Query(value="{$and: [{'owner':?0},{'deleted': false}]}", fields = fieldFilter)
    Flux<Job> findJobsForUser(String owner, Pageable pageable);

    @Query(value="{$and: [{'owner':?0},{'deleted': false}]}", count = true)
    Mono<Long> getJobCountForUser(String owner);
}
