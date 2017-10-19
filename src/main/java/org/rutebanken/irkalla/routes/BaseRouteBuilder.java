/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

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
