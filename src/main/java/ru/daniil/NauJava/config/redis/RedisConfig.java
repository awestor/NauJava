package ru.daniil.NauJava.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.timeout:2000}")
    private long redisTimeout;

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        logger.info("Конфигурация Redis Подключение к {}:{} (database: {})",
                redisHost, redisPort, redisDatabase);

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);

        if (StringUtils.hasText(redisPassword)) {
            config.setPassword(RedisPassword.of(redisPassword));
            logger.info("Redis password сконфигурирован");
        }

        return config;
    }

    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration() {
        logger.info("Конфигурация Redis клиента на timeout: {} мс", redisTimeout);

        return LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(redisTimeout))
                .shutdownTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            RedisStandaloneConfiguration redisStandaloneConfiguration,
            LettuceClientConfiguration lettuceClientConfiguration) {

        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redisStandaloneConfiguration,
                lettuceClientConfiguration
        );

        factory.afterPropertiesSet();
        logger.info("Фабрика подключений Redis создана");

        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        logger.info("Создание RedisTemplate");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper());

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        logger.info("RedisTemplate сконфигурирован с JSON сериализацией");
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        logger.info("Конфигурация Redis CacheManager");

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())))
                .prefixCacheNameWith("cache:");

        // Настройки для разных кэшей
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("admin-reports-page", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("admin-users-list", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("admin-users-stats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("calendar-month", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("daily-reports", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("user-products", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("meal-type", defaultConfig.entryTtl(Duration.ofHours(1)));

        logger.info("Кэш конфигурация: {}", cacheConfigurations.keySet());

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}