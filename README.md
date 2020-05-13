# fdk-dataservice-harvester

fdk-dataservice-harvester will harvest catalogs of dataservices according to the upcoming [DCAT-AP-NO v.0 specification](https://informasjonsforvaltning.github.io/dcat-ap-no/).

The catalogs will then be stored and made available at a standardized endpoint.

## Requirements
- maven
- java 8
- docker
- docker-compose

## Run tests
```
% mvn verify
```

## Run locally
```
docker-compose up -d
```

Then in another terminal e.g.
```
% curl http://localhost:8081/catalogs
% curl http://localhost:8081/dataservices
```

## Datastore
To inspect the Fuseki triple store, open your browser at http://localhost:3030/fuseki/
