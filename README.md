# Master Thesis Case Study - REST Microservice

[![Build Status](https://drone.florianbeetz.de/api/badges/Uni/master-thesis-rest/status.svg)](https://drone.florianbeetz.de/Uni/master-thesis-rest)

This repository contains a small sample project I am developing as a part of my
[Master Thesis](https://cloud.florianbeetz.de/s/6pbS45PAQxt7ep4/download).
The goal of the master thesis is to evaluate architectural differences of microservice architectures when using REST or
GraphQL as API models.

This repository contains the REST implementation of the case study.

## Architecture Overview

This example project models a small online shop. 
The architecture consists of four microservices, responsible for processing orders, managing inventory, processing
payments, and shipping.

## Building and Running the Project

The easiest way to build and run the project is with a Docker installation available.

1. Create the file `.env` in the root directory of the project. 
   Use `.env.template` to see an example of the contents.
   You can leave the `*_CLIENT_SECRET` keys out for now.
2. Run `docker-compose up -d` to start up the project.
   If the images are already available locally, add the `--build` flag to force rebuilding the images.
3. Wait until the service `keycloak` is available (you can check availability at [the Traefik Dashboard](http://host.docker.internal:8090/dashboard/#/http/services)), then navigate a browser to [`http://host.docker.internal:8080/auth/`](http://host.docker.internal:8080/auth/).
4. Click on *Administration Console* and log in with the credentials specified in `.env`.
   Then import the project realm by hovering over *Master* and clicking on *Add realm*.
   Select the file `setup/realm-export.json` and click *Create*.
5. Create a user by clicking on *Users* and then *Add User*.
   Specify the username of the user and then click *Save*.
   Set the credentials of the user by selecting the *Credentials* tab.
   Enter the password twice, disable the *Temporary* switch and click *Set Password*.
   **Optional**: Assign the user a role by selecting the tab *Role Mappings*, select a role the user should be assigned (e.g. *admin*) and click *Add selected >*.
6. Set up the secrets of the services by clicking on *Clients*.
   For each client (*inventory*, *order*, *payment* and *shipping*), click on the respective client, select the tab *Credentials* and copy the secret.
   If the secret hidden with asterisks, click *Regenerate Secret*.
   Configure the copied secret in the `.env` file for the respective service.
7. Restart the reconfigured services with `docker-compose up -d`.

The services are now up and running.
An access token can be obtained with the following request by using the client *external* and the credentials of the user you created.

```http request 
POST http://host.docker.internal:8080/auth/realms/ma-rest-shop/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=external&client_secret=<secret>&username=admin&password=<password>
```

The access token can be specified with the `Bearer` scheme in the HTTP `Authorization` header, or using SwaggerUI.
SwaggerUI is available at the following locations:

* http://host.docker.internal:8080/inventory/swagger-ui.html
* http://host.docker.internal:8080/order/swagger-ui.html
* http://host.docker.internal:8080/payment/swagger-ui.html
* http://host.docker.internal:8080/shipping/swagger-ui.html