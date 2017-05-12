package org.rutebanken.irkalla.routes.tiamat;


import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TiamatStopPlaceChangedRouteBuilder extends BaseRouteBuilder {


    @Override
    public void configure() throws Exception {
        super.configure();

        from("direct:handleStopPlaceChanged")
                .bean("stopPlaceDao", "getStopPlaceChange")
                .bean("stopPlaceChangedToEvent", "toEvent")
                .convertBodyTo(String.class)
                .process(e->
                toString())
                .to("activemq:queue:NabuEvent")
                .routeId("tiamat-stop-place-changed");

    }


}
