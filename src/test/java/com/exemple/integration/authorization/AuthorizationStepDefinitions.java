package com.exemple.integration.authorization;

import static com.exemple.integration.core.InitData.TEST_APP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Transpose;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class AuthorizationStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AuthorizationTestContext context;

    @Autowired
    private KafkaConsumer<String, Map<String, Object>> consumerNewPassword;

    @Given("connection to client {string} and scopes")
    public void tokenByClientCredentials(String client, @Transpose DataTable scopes) {

        Response response = AuthorizationApiClient.authorizationByCredentials(client, "secret", scopes.column(0));

        assertThat(response.getStatusCode()).as("connection to client %s fails", client).isEqualTo(200);

        var accessToken = response.jsonPath().getString("access_token");
        var session = response.getDetailedCookie("JSESSIONID");
        var xsrfToken = response.getDetailedCookie("XSRF-TOKEN");

        context.saveAccessToken(accessToken);
        context.saveSession(session);
        context.saveXsrfToken(xsrfToken);
        context.save(response);

    }

    @When("connection with username {string} and password {string} to client {string} and scopes")
    public void connect(String username, String password, String client, @Transpose DataTable scopes) {

        Response response = AuthorizationApiClient.authorizationByCode(
                username,
                password,
                client,
                "secret",
                scopes.column(0),
                context);

        var accessToken = response.jsonPath().getString("access_token");
        var session = response.getDetailedCookie("JSESSIONID");
        var xsrfToken = response.getDetailedCookie("XSRF-TOKEN");

        context.saveAccessToken(accessToken);
        context.saveSession(session);
        context.saveXsrfToken(xsrfToken);
        context.save(response);

    }

    @When("connection with username {string} and password {string} to client {string} and application {string} and scopes")
    public void connect(String username, String password, String client, String application, @Transpose DataTable scopes) {

        Response response = AuthorizationApiClient.authorizationByCode(
                username,
                password,
                client,
                "secret",
                scopes.column(0),
                context,
                application);

        var accessToken = response.jsonPath().getString("access_token");
        var session = response.getDetailedCookie("JSESSIONID");
        var xsrfToken = response.getDetailedCookie("XSRF-TOKEN");

        context.saveAccessToken(accessToken);
        context.saveSession(session);
        context.saveXsrfToken(xsrfToken);
        context.save(response);

    }

    @And("login with username {string} and password {string} to application {string}")
    public void login(String username, String password, String application) {

        Response response = AuthorizationApiClient.login(
                username,
                password,
                context,
                application);

        context.save(response);

    }

    @When("disconnection")
    public void disconnect() {

        Response response = AuthorizationApiClient.disconnection(context);

        context.save(response);

    }

    @When("new password for {string}")
    public void forgottenPassword(String username) {

        Response response = AuthorizationApiClient.password(username, context, TEST_APP);

        ConsumerRecords<String, Map<String, Object>> records = consumerNewPassword.poll(Duration.ofSeconds(5));

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(records).extracting(ConsumerRecord::value).anyMatch(value -> value.containsKey("token")));

        String token = records.iterator().next().value().get("token").toString();

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("connection error is")
    public void checkError(JsonNode body) {

        assertThat(MAPPER.readTree(context.lastResponse().asString())).isEqualTo(body);

    }

    @And("login is unauthorized")
    public void checkUnauthorized() {

        assertThat(context.lastResponse().getStatusCode()).as("connection is authorized").isEqualTo(401);

    }

    @And("create authorization login {string}")
    public void createLogin(String username, JsonNode body) {

        Response response = AuthorizationApiClient.putLogin(username, body, context, TEST_APP);

        assertThat(response.getStatusCode()).as("failure authorization %s", body.toPrettyString()).isEqualTo(201);

    }

    @When("put login {string}")
    public void putLogin(String username, JsonNode body) {

        Response response = AuthorizationApiClient.putLogin(username, body, context, TEST_APP);

        context.save(response);

    }

    @When("move authorization login from {string} to {string}")
    public void copy(String fromUsername, String toUsername) {

        ObjectNode body = MAPPER.createObjectNode();
        body.put("fromUsername", fromUsername);
        body.put("toUsername", toUsername);

        Response response = AuthorizationApiClient.moveLogin(body, context, TEST_APP);

        context.save(response);

    }

    @And("authorization error only contains")
    public void checkOnlyError(JsonNode body) {

        checkCountError(1);
        checkErrors(body);
    }

    @And("authorizatioh error contains {int} errors")
    public void checkCountError(int count) {

        assertThat(context.lastResponse().getStatusCode()).as("account has not error").isEqualTo(400);

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());

        assertThat(errors.elements().stream()).as("errors %s not contain expected errors", errors.toPrettyString()).hasSize(count);

    }

    @And("authorization error contains")
    public void checkErrors(JsonNode body) {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());
        assertThat(errors).as("errors {} not contain {}", errors.toPrettyString(), body.toPrettyString())
                .anySatisfy(error -> {
                    Iterator<Map.Entry<String, JsonNode>> expectedErrors = body.properties().iterator();
                    while (expectedErrors.hasNext()) {
                        Map.Entry<String, JsonNode> expectedError = expectedErrors.next();
                        assertThat(error.get(expectedError.getKey())).isEqualTo(expectedError.getValue());
                    }
                });
    }

}
