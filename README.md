# irkalla [![CircleCI](https://circleci.com/gh/entur/irkalla/tree/master.svg?style=svg)](https://circleci.com/gh/entur/irkalla/tree/master)
Will distribute information about stop change between tiamat, chouette and nabu. 


## Build
`mvn clean install`

## Run locally (without kubernetes)

```
server.port=10501

server.admin.host=0.0.0.0
server.admin.port=11501

server.context-path=/irkalla/

spring.activemq.broker-url=tcp://activemq:61616
tiamat.url=http://tiamat:2888
chouette.url=http://localhost:8080
etcd.url=http://etcd-client:2379/v2/keys/prod/irkalla

rutebanken.kubernetes.enabled=false
chouette.sync.stop.place.autoStartup=true

spring.jackson.serialization.write-dates-as-timestamps=false