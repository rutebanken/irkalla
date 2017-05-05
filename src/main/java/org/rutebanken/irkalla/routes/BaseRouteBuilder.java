package org.rutebanken.irkalla.routes;

import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.SpringRouteBuilder;

import static org.rutebanken.irkalla.Constants.SINGLETON_ROUTE_DEFINITION_GROUP_NAME;

public abstract class BaseRouteBuilder extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        errorHandler(transactionErrorHandler()
                             .logExhausted(true)
                             .logRetryStackTrace(true));
    }

    /**
     * Create a new singleton route definition from URI. Only one such route should be active throughout the cluster at any time.
     */
    protected RouteDefinition singletonFrom(String uri) {
        return this.from(uri).group(SINGLETON_ROUTE_DEFINITION_GROUP_NAME);
    }
}
