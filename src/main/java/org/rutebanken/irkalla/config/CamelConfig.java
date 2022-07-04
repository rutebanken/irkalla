package org.rutebanken.irkalla.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.impl.engine.MemoryStateRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {
    @Bean("jacksonJavaTimeModule")
    public JavaTimeModule jacksonJavaTimeModule() {
        return new JavaTimeModule();
    }

    /**
     * Store the offset repository for the import event topic.
     * The store is not persistent, pods read the topic from the beginning at startup time.
     *
     * @return a memory-only store for the topic offset.
     */
    @Bean
    public MemoryStateRepository irkallaEventReaderOffsetRepo() {
        return new MemoryStateRepository();
    }

}
