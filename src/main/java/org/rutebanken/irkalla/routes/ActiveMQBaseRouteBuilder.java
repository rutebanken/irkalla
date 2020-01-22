package org.rutebanken.irkalla.routes;

import org.apache.camel.spring.SpringRouteBuilder;

public abstract class ActiveMQBaseRouteBuilder extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        errorHandler(transactionErrorHandler()
                .logExhausted(true)
                .logRetryStackTrace(true));
    }
    
}