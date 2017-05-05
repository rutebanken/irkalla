package org.rutebanken.irkalla.service;

import org.rutebanken.hazelcasthelper.service.KubernetesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class IrkallaHazelcastConfiguration extends KubernetesService {
    private static final Logger log = LoggerFactory.getLogger(IrkallaHazelcastConfiguration.class);

    public IrkallaHazelcastConfiguration(@Value("${rutebanken.kubernetes.url:}") String kubernetesUrl,
                                          @Value("${rutebanken.kubernetes.namespace:default}") String namespace,
                                          @Value("${rutebanken.kubernetes.enabled:true}") boolean kubernetesEnabled) {
        super(kubernetesUrl, namespace, kubernetesEnabled);
    }


}
