# irkalla [![CircleCI](https://circleci.com/gh/entur/irkalla/tree/master.svg?style=svg)](https://circleci.com/gh/entur/irkalla/tree/master)
Propagate changes from the [Norwegian Stop Place Register](https://stoppested.entur.org) into the [Routes database](https://rutedb.dev.entur.org/).

Irkalla monitors changes in [Tiamat](https://github.com/entur/tiamat) by querying its graphQL API and replicate the changes into [Chouette](https://github.com/entur/chouette) through its Web service API.

Irkalla sends also notifications to [Nabu](https://github.com/entur/nabu) when changes are detected.


## Build
`mvn clean install`

## Run locally (without kubernetes)

```
server.port=10501

server.admin.host=0.0.0.0
server.admin.port=11501

server.context-path=/irkalla/

irkalla.security.user-context-service=full-access


tiamat.url=http://tiamat:2888
chouette.url=http://localhost:8080
etcd.url=http://etcd-client:2379/v2/keys/prod/irkalla

rutebanken.kubernetes.enabled=false
chouette.sync.stop.place.autoStartup=true

spring.jackson.serialization.write-dates-as-timestamps=false

```

## Security
An authorization service implementation must be selected.
The following implementation gives full access to all authenticated users:

```properties
irkalla.security.user-context-service=full-access
```

The following implementation enables OAuth2 token-based authorization:
```properties
irkalla.security.user-context-service=token-based
```
