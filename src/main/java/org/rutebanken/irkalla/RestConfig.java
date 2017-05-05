package org.rutebanken.irkalla;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.rutebanken.irkalla.filter.CorsResponseFilter;
import org.rutebanken.irkalla.rest.HealthResource;
import org.rutebanken.irkalla.rest.IndexPageResource;
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
        registerProperties();
        registerEndpoints();
    }

    private void registerProperties() {
        String templatePath = System.getProperty( "template.path" );
        if ( templatePath != null ) {
            log.info("Overriding TEMPLATE_BASE_PATH with "+templatePath );
            property(FreemarkerMvcFeature.TEMPLATE_BASE_PATH, templatePath );
        }
    }

    private void registerEndpoints() {
        register(CorsResponseFilter.class);
        register(LoggingFeature.class);
        register(FreemarkerMvcFeature.class);
        register(IndexPageResource.class);
        register(HealthResource.class);
    }
}
