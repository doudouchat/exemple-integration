[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=doudouchat_exemple-integration&metric=alert_status)](https://sonarcloud.io/dashboard?id=doudouchat_exemple-integration)
[![build](https://github.com/doudouchat/exemple-integration/workflows/build/badge.svg)](https://github.com/doudouchat/exemple-integration/actions)
[![codecov](https://codecov.io/gh/doudouchat/exemple-integration/graph/badge.svg)](https://codecov.io/gh/doudouchat/exemple-integration)

# exemple-integration

## maven

<p>execute with docker<code>mvn clean verify -Pwebservice,it</code></p>

<p>execute without docker<code>mvn clean verify -Pit -Dauthorization.port=8090 -Dservice.port=8080 -Dgateway.port=8086 -Dzookeeper.port=10024 -Dhazelcast.port=5706</code></p>
