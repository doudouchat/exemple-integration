FROM cassandra:3.11.6
LABEL maintener=EXEMPLE
RUN rm ${CASSANDRA_CONFIG}/logback.xml
RUN ln -s /usr/local/etc/cassandra/logback.xml ${CASSANDRA_CONFIG}/logback.xml