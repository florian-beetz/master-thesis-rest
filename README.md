# Master Thesis Case Study -- REST Microservice

This repository contains a small sample project I am developing as a part of my
[Master Thesis](https://cloud.florianbeetz.de/s/6pbS45PAQxt7ep4/download).
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

## API Documentation

Each service provides a OpenAPI 3 specification at `/inventory/api/docs` (prefix varies depending on the service).
SwaggerUI is also available for the specifications at `/inventory/swagger-ui.html` (prefix varies).

To get up and running, just execute the following requests to create the basic required resources.

```http request
# curl -X POST "http://host.docker.internal:8080/inventory/api/v1/warehouse/" -H "accept: application/hal+json" -H "Content-Type: application/json" -d "{\"name\":\"Warehouse 1\"}"
POST http://host.docker.internal:8080/inventory/api/v1/warehouse/
accept: application/hal+json
Content-Type: application/json

{"name":"Warehouse 1"}
``` 

```http request
# curl -X POST "http://host.docker.internal:8080/inventory/api/v1/item/" -H "accept: application/hal+json" -H "Content-Type: application/json" -d "{\"title\":\"Item 1\",\"price\":3.5}"
POST http://host.docker.internal:8080/inventory/api/v1/item/
accept: application/hal+json
Content-Type: application/json

{
  "title": "Item 1",
  "price": 3.5
}
```

```http request
# curl -X POST "http://host.docker.internal:8080/inventory/api/v1/item/4/stock/" -H "accept: application/hal+json" -H "Content-Type: application/json" -d "{\"available\":20,\"inStock\":20,\"warehouse\":\"http://host.docker.internal:8080/inventory/api/v1/warehouse/1\"}"
POST http://host.docker.internal:8080/inventory/api/v1/item/2/stock/
accept: application/hal+json
Content-Type: application/json

{
  "available": 20,
  "inStock": 20,
  "warehouse": "http://host.docker.internal:8080/inventory/api/v1/warehouse/1"
}
```

```http request
# curl -X POST "http://host.docker.internal:8080/order/api/v1/order/" -H "accept: application/hal+json" -H "Content-Type: application/json" -d "{\"items\":[{\"item\":\"http://host.docker.internal:8080/inventory/api/v1/item/2\",\"amount\":1}],\"status\":\"created\",\"address\":{\"street\":\"Luitpoldstr. 1\",\"city\":\"Bamberg\",\"zip\":\"96052\"}}"
POST http://host.docker.internal:8080/order/api/v1/order/
accept: */*
Content-Type: application/json

{
  "items": [
    {
      "item": "http://host.docker.internal:8080/inventory/api/v1/item/2",
      "amount": 1
    }
  ],
  "status": "created",
  "address": {
    "street": "Luitpoldstr. 1",
    "city": "Bamberg",
    "zip": "96052"
  }
}
```

After executing these requests, a shipment and a payment will be automatically created for the order.