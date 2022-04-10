package com.exemple.integration.authorization;

import static com.exemple.integration.core.InitData.TEST_APP;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class AuthorizationStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AuthorizationTestContext context;

    @Given("connection to client {string}")
    public void connect(String client) {

        Response response = AuthorizationApiClient.authorizationByCredentials(client, "secret");

        assertThat(response.getStatusCode()).as("connection to client %s fails", client).isEqualTo(200);

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @When("connection with username {string} and password {string}")
    public void connect(String username, String password) {

        Response response = AuthorizationApiClient.authorizationByPassword(username, password, "test_service_user", "secret");

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("get access for username {string} and password {string} to client {string}")
    public void getAccessToken(String username, String password, String client) {

        Response response = AuthorizationApiClient.authorizationByPassword(username, password, client, "secret");

        assertThat(response.getStatusCode()).as("connection with username %s and password %s to client %s fails", username, password, client)
                .isEqualTo(200);

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);

    }

    @And("get access for username {string} and password {string}")
    public void getAccessToken(String username, String password) {

        getAccessToken(username, password, "test_service_user");

    }

    @When("disconnection")
    public void disconnect() {

        Response response = AuthorizationApiClient.disconnection(context.lastAccessToken(), TEST_APP);

        context.save(response);

    }

    @When("new password for {string} from application {string}")
    public void forgottenPassword(String username, String application) {

        Response response = AuthorizationApiClient.password(username, context.lastAccessToken(), application);

        String token = response.jsonPath().getString("token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("connection error is")
    public void checkError(JsonNode body) throws IOException {

        assertThat(MAPPER.readTree(context.lastResponse().asString())).isEqualTo(body);

    }

    @And("connection is unauthorized")
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

    @When("put login {string} for application {string}")
    public void putLogin(String username, String application, JsonNode body) {

        Response response = AuthorizationApiClient.putLogin(username, body, context.lastAccessToken(), application);

        context.save(response);

    }

    @When("move authorization login from {string} to {string}")
    public void copy(String fromUsername, String toUsername) {

        ObjectNode body = MAPPER.createObjectNode();
        body.put("fromUsername", fromUsername);
        body.put("toUsername", toUsername);

        Response responseCopy = AuthorizationApiClient.copyLogin(body, context.lastAccessToken(), TEST_APP);
        AuthorizationApiClient.deleteLogin(fromUsername, context.lastAccessToken(), TEST_APP);

        context.save(responseCopy);

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

        assertThat(Streams.stream(errors.elements())).as("errors %s not contain expected errors", errors.toPrettyString()).hasSize(count);

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
