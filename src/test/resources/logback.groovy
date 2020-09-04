import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN

import java.nio.charset.Charset

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.jul.LevelChangePropagator
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

scan("30 seconds")

def USER_HOME = "${project.build.directory}"
def LOGS_FOLDER = "${USER_HOME}/.logs"
def LOG_ARCHIVE = "${LOGS_FOLDER}/archive"
def LOGS_FILENAME = "exemple"

def lcp = new LevelChangePropagator()
lcp.context = context
lcp.resetJUL = true
context.addListener(lcp)

appender("console", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d %-5p: %C - %m%n"
        charset =  Charset.forName("UTF-8")
    }
}

//appender("file", FileAppender) {
//    file = "${LOGS_FOLDER}/${LOGS_FILENAME}.log"
//    encoder(PatternLayoutEncoder) {
//        pattern = "%d %-5p: %C - %m%n"
//    }
//}

appender("archive", RollingFileAppender) {
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${LOG_ARCHIVE}/${LOGS_FILENAME}.%d{yyyy-MM-dd}.log"
        maxHistory = 2
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d %-5p: %C - %m%n"
        charset =  Charset.forName("UTF-8")
    }
}

logger("org.apache.cassandra", WARN)

logger("com.datastax.oss.driver", INFO)
logger("com.hazelcast", INFO)
logger("org.apache.zookeeper", INFO)
logger("com.github.nosan.embedded.cassandra.EmbeddedCassandra", INFO)
logger("org.springframework.boot", INFO)

logger("com.exemple", DEBUG)
logger("org.glassfish.jersey.logging", DEBUG)

root(WARN, ["console", "archive"])