package com.exemple.integration;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

public final class JsonRestTemplate {

    public static final int TIMEOUT_CONNECTION = 3_000_000;

    public static final int TIMEOUT_SOCKET = 3_000_000;

    public static final String SERVICE_URL = System.getProperty("service.host", "http://localhost") + ":"
            + System.getProperty("service.port", "8080") + "/" + System.getProperty("service.contextpath", "ExempleService");

    private static final Logger LOG = LoggerFactory.getLogger(JsonRestTemplate.class);

    private static final RestAssuredConfig CONFIG;

    private static final AtomicInteger COUNTER;

    static {

        CONFIG = RestAssured.config().httpClient(HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", TIMEOUT_CONNECTION)
                .setParam("http.socket.timeout", TIMEOUT_SOCKET));

        COUNTER = new AtomicInteger();
    }

    private JsonRestTemplate() {

    }

    public static RequestSpecification given() {

        return given(SERVICE_URL, ContentType.JSON);
    }
    

    public static RequestSpecification given(ContentType contentType) {

        return given(SERVICE_URL, contentType);
    }

    public static RequestSpecification given(String path, ContentType contentType) {

        return RestAssured.given().filters(new LoggingFilter(), new PathFilter(path)).contentType(contentType).config(CONFIG);
    }

    private static class PathFilter implements OrderedFilter {

        private final String path;

        public PathFilter(String path) {
            this.path = path;
        }

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }

        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            requestSpec.path(path + requestSpec.getUserDefinedPath());

            return ctx.next(requestSpec, responseSpec);
        }

    }

    private static class LoggingFilter implements Filter {

        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {

            int counter = COUNTER.incrementAndGet();

            String requestLog = RequestPrinter.print(requestSpec, requestSpec.getMethod(), requestSpec.getURI(), LogDetail.ALL,
                    Collections.emptySet(), new PrintStream(OutputStream.nullOutputStream()), true);
            LOG.debug("Request {}\n{}", counter, requestLog);

            var start = OffsetDateTime.now();

            var response = ctx.next(requestSpec, responseSpec);

            var end = OffsetDateTime.now();

            long duration = ChronoUnit.MILLIS.between(start, end);

            String responseLog = ResponsePrinter.print(response, response, new PrintStream(OutputStream.nullOutputStream()), LogDetail.ALL, true,
                    Collections.emptySet());
            LOG.debug("Response {} {}ms\n{}", counter, duration, responseLog);

            return response;
        }

    }

}
