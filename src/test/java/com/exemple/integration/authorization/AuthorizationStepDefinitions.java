package com.exemple.integration.authorization;

import static com.exemple.integration.core.InitData.TEST_APP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Transpose;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

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

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
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
                context.lastAccessToken());

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
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
                context.lastAccessToken(),
                application);

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("login with username {string} and password {string} to application {string}")
    public void login(String username, String password, String application) {

        Response response = AuthorizationApiClient.login(
                username,
                password,
                context.lastAccessToken(),
                application);

        context.save(response);

    }

    @When("disconnection")
    public void disconnect() {

        Response response = AuthorizationApiClient.disconnection(context.lastAccessToken(), TEST_APP);

        context.save(response);

    }

    @When("new password for {string}")
    public void forgottenPassword(String username) {

        Response response = AuthorizationApiClient.password(username, context.lastAccessToken(), TEST_APP);

        ConsumerRecords<String, Map<String, Object>> records = consumerNewPassword.poll(Duration.ofSeconds(5));

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(records).extracting(ConsumerRecord::value).anyMatch(value -> value.containsKey("token")));

        String token = records.iterator().next().value().get("token").toString();

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("connection error is")
    public void checkError(JsonNode body) throws IOException {

        assertThat(MAPPER.readTree(context.lastResponse().asString())).isEqualTo(body);

    }

    @And("login is unauthorized")
    public void checkUnauthorized() {

        assertThat(context.lastResponse().getStatusCode()).as("connection is authorized").isEqualTo(401);

    }

    @And("create authorization login {string}")
    public void createLogin(String username, JsonNode body) {

        Response response = AuthorizationApiClient.putLogin(username, body, context.lastAccessToken(), TEST_APP);

        assertThat(response.getStatusCode()).as("failure authorization %s", body.toPrettyString()).isEqualTo(201);

    }

    @When("put login {string}")
    public void putLogin(String username, JsonNode body) {

        Response response = AuthorizationApiClient.putLogin(username, body, context.lastAccessToken(), TEST_APP);

        context.save(response);

    }

    @When("move authorization login from {string} to {string}")
    public void copy(String fromUsername, String toUsername) {

        ObjectNode body = MAPPER.createObjectNode();
        body.put("fromUsername", fromUsername);
        body.put("toUsername", toUsername);

        Response response = AuthorizationApiClient.moveLogin(body, context.lastAccessToken(), TEST_APP);

        context.save(response);

    }

    @And("authorization error only contains")
    public void checkOnlyError(JsonNode body) throws IOException {

        checkCountError(1);
        checkErrors(body);
    }

    @And("authorizatioh error contains {int} errors")
    public void checkCountError(int count) throws IOException {

        assertThat(context.lastResponse().getStatusCode()).as("account has not error").isEqualTo(400);

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());

        assertThat(errors.elements()).as("errors %s not contain expected errors", errors.toPrettyString()).toIterable().hasSize(count);

    }

    @And("authorization error contains")
    public void checkErrors(JsonNode body) throws IOException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());
        assertThat(errors).as("errors %s not contain %s", errors.toPrettyString(), body.toPrettyString())
                .anySatisfy(error -> {
                    Iterator<Map.Entry<String, JsonNode>> expectedErrors = body.fields();
                    while (expectedErrors.hasNext()) {
                        Map.Entry<String, JsonNode> expectedError = expectedErrors.next();
                        assertThat(error.get(expectedError.getKey())).isEqualTo(expectedError.getValue());
                    }
                });
    }

}
