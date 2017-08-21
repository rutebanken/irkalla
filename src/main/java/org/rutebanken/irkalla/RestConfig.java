package org.rutebanken.irkalla;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.rutebanken.irkalla.filter.CorsResponseFilter;
import org.rutebanken.irkalla.rest.HealthResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class RestConfig extends ResourceConfig {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public RestConfig() {
        registerEndpoints();
    }

    private void registerEndpoints() {
        register(CorsResponseFilter.class);
        register(LoggingFeature.class);
        register(HealthResource.class);
    }
}
