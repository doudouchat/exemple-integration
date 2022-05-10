FROM cassandra:3.11.12
LABEL maintener=EXEMPLE
RUN rm /etc/cassandra/logback.xml
RUN ln -s /usr/local/etc/cassandra/logback.xml /etc/cassandra/logback.xml