# Master Thesis Case Study -- REST Microservice

This repository contains a small sample project I am developing as a part of my
[Master Thesis](https://cloud.florianbeetz.de/s/6pbS45PAQxt7ep4).
The goal of the master thesis is to evaluate architectural differences of microservice architectures when using REST or
GraphQL as API models.

This repository contains the REST implementation of the case study.

## Architecture Overview

This example project models a small online shop. 
The architecture consists of four microservices, responsible for processing orders, managing inventory, processing
payments, and shipping.

## Building

Just use `docker-compose up -d` and the project works out of the box.

If you want to build the services stand-alone, navigate to the directory of the microservice (e.g. `inventory-service`)
and execute `gradlew build`.
This will generate an executable fat-jar in `build/libs`.

```bash
cd inventory-service
gradlew build
java -jar build/libs/inventory-service-1.0-SNAPSHOT.jar
```

Running the fat-jars requires a Java 14 compatible JRE, and a configured PostgreSQL database.
The connection data can be specified via environment variables (the prefix varies depending on the service).

* `INVENTORY_DB_HOST`
* `INVENTORY_DB_PORT`
* `INVENTORY_DB_DATABASE`
* `INVENTORY_DB_USER`
* `INVENTORY_DB_PASSWORD`