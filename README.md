[![Build Status](https://travis-ci.com/doudouchat/exemple-integration.svg?branch=master)](https://travis-ci.org/doudouchat/exemple-integration)

# exemple-integration

## maven

<p>execute with cargo and cassandra <code>mvn clean verify -Pwebservice,it</code></p>

<p>execute without cargo and cassandra <code>mvn clean verify -Pit -Dauthorization.port=8084 -Dapplication.port=8080</code></p>

## Docker

<ol>
<li>docker build -t exemple-test .</li>
</ol>

<ol>
<li>docker-compose up -d zookeeper</li>
<li>docker-compose up -d cassandra</li>
<li>docker container exec exemple-test cqlsh --debug -f /usr/local/tmp/cassandra/schema.cql</li>
<li>docker container exec exemple-test cqlsh --debug -f /usr/local/tmp/cassandra/exec.cql</li>
</ol>
