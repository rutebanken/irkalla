package org.rutebanken.irkalla.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
public class IrkallaConfiguration {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${neti.link.prefix:}")
    @NotNull
    private String linkPrefix;

    public String getLinkPrefix() {
        return linkPrefix;
    }
}
