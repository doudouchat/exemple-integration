[![build](https://github.com/doudouchat/exemple-integration/workflows/build/badge.svg)](https://github.com/doudouchat/exemple-integration/actions)
[![Coverage Status](https://coveralls.io/repos/github/doudouchat/exemple-integration/badge.svg?branch=master)](https://coveralls.io/github/doudouchat/exemple-integration?branch=master)

# exemple-integration

## maven

<p>execute with cargo and cassandra <code>mvn clean verify -Pwebservice,it</code></p>

<p>execute without cargo and cassandra <code>mvn clean verify -Pit -Dauthorization.port=8086 -Dapplication.port=8086</code></p>

## Docker

<ol>
<li>docker build -t exemple-test .</li>
</ol>

<ol>
<li>docker-compose up -d zookeeper</li>
<li>docker container exec exemple-zookeeper /usr/local/etc/zookeeper/load_authorization.sh</li>
<li>docker-compose up -d cassandra</li>
<li>docker container exec exemple-test cqlsh --debug -f /usr/local/tmp/cassandra/schema.cql</li>
<li>docker container exec exemple-test cqlsh --debug -f /usr/local/tmp/cassandra/exec.cql</li>
<li>docker-compose up -d hazelcast</li>
</ol>
