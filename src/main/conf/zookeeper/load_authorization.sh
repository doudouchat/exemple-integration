#!/bin/bash

./bin/zkCli.sh create /authorization
./bin/zkCli.sh create /authorization/resource
./bin/zkCli.sh set /authorization/resource '{"client_id":"resource","client_secret":"{bcrypt}$2a$10$I3FyNzNSYN5R5U.4GOWpp.0Np33uJ59p412xzdit798c1jJHCsPja","authorized_grant_types":["client_credentials"],"redirect_uri":[],"autoapprove":[],"authorities":["ROLE_TRUSTED_CLIENT"]}'
