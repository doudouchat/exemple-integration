[![build](https://github.com/doudouchat/exemple-integration/workflows/build/badge.svg)](https://github.com/doudouchat/exemple-integration/actions)
[![Coverage Status](https://coveralls.io/repos/github/doudouchat/exemple-integration/badge.svg?branch=master)](https://coveralls.io/github/doudouchat/exemple-integration?branch=master)

# exemple-integration

## maven

<p>execute with docker and cassandra <code>mvn clean verify -Pwebservice,it</code></p>

<p>execute without docker and cassandra <code>mvn clean verify -Pit -Dauthorization.port=8086 -Dservice.port=8086</code></p>