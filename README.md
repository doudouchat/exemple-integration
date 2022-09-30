[![build](https://github.com/doudouchat/exemple-integration/workflows/build/badge.svg)](https://github.com/doudouchat/exemple-integration/actions)
[![Coverage Status](https://coveralls.io/repos/github/doudouchat/exemple-integration/badge.svg?branch=master)](https://coveralls.io/github/doudouchat/exemple-integration?branch=master)

# exemple-integration

## maven

<p>execute with docker<code>mvn clean verify -Pwebservice,it</code></p>

<p>execute without docker<code>mvn clean verify -Pit -Dauthorization.port=8090 -Dservice.port=8080 -Dgateway.port=8086 -Dzookeeper.port=10024 -Dhazelcast.port=5706</code></p>