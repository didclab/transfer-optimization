
package org.onedatashare.transfer.config;

import org.onedatashare.transfer.model.TransferDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PreDestroy;

@Configuration
public class RedisConfiguration {

    @Bean
    ReactiveRedisOperations<String, TransferDetails> redisOperations(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<TransferDetails> serializer = new Jackson2JsonRedisSerializer<>(TransferDetails.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, TransferDetails> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, TransferDetails> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}