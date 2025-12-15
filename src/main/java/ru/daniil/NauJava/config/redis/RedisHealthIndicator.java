package ru.daniil.NauJava.config.redis;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {

            String pong = connection.ping();

            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("message", "Redis is up and running")
                        .withDetail("host", connection.getNativeConnection())
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", "Redis responded with: " + pong)
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("message", "Cannot connect to Redis")
                    .withException(e)
                    .build();
        }
    }
}
