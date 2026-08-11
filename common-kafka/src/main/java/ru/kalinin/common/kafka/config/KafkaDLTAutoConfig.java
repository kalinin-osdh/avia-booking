package ru.kalinin.common.kafka.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@AutoConfiguration
public class KafkaDLTAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> kafkaTemplate
    ){
        return new DeadLetterPublishingRecoverer(kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer){
        FixedBackOff fixedBackOff = new FixedBackOff(
                1000L, // timeout
                2L // retry count
        );

        return new DefaultErrorHandler(
                recoverer,
                fixedBackOff
        );
    }
}
