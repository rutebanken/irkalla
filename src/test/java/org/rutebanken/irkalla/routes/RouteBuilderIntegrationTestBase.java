package org.rutebanken.irkalla.routes;

import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.runner.RunWith;
import org.rutebanken.irkalla.IrkallaApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(CamelSpringRunner.class)
@UseAdviceWith
@SpringBootTest(classes = IrkallaApplication.class)
public class RouteBuilderIntegrationTestBase {

    @Autowired
    protected ModelCamelContext context;

}
