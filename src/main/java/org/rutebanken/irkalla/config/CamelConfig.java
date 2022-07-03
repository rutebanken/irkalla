package org.rutebanken.irkalla.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {
    @Bean("jacksonJavaTimeModule")
    public JavaTimeModule jacksonJavaTimeModule() {
        return new JavaTimeModule();
    }

}
