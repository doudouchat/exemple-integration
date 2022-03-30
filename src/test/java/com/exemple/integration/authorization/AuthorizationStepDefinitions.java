package com.exemple.integration.authorization;

import static com.exemple.integration.core.InitData.TEST_APP;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;

public class AuthorizationStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AuthorizationTestContext context;

    @Given("connection to client {string}")
    public void connect(String client) {

        Response response = AuthorizationApiClient.authorizationByCredentials(client, "secret");

        assertThat(response.getStatusCode()).isEqualTo(200);

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("connection with username {string} and password {string} to client {string}")
    public void connect(String username, String password, String client) {

        Response response = AuthorizationApiClient.authorizationByPassword(username, password, client, "secret");

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @Given("disconnection")
    public void disconnect() {

        Response response = AuthorizationApiClient.disconnection(context.lastAccessToken(), TEST_APP);

        context.save(response);

    }

    @Given("new password for {string} from application {string}")
    public void forgottenPassword(String username, String application) {

        Response response = AuthorizationApiClient.password(username, context.lastAccessToken(), application);

        String token = response.jsonPath().getString("token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("connection with username {string} and password {string} to client {string} with scopes {string}")
    public void connect(String username, String password, String client, String scopes) {

        Response response = AuthorizationApiClient.authorizationByCode(username, password, client, "secret", scopes, context.lastAccessToken());

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("connection error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        assertThat(MAPPER.readTree(context.lastResponse().asString())).isEqualTo(body);

    }

}
