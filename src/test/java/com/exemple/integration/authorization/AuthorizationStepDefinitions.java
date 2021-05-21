package com.exemple.integration.authorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @Given("connection with username {string} and password {string} to client {string}")
    public void connect(String username, String password, String client) {

        Response response = AuthorizationApiClient.authorizationByPassword(username, password, client, "secret");

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @Given("disconnection from application {string}")
    public void disconnect(String application) {

        Response response = AuthorizationApiClient.disconnection(context.lastAccessToken(), application);

        context.save(response);

    }

    @Given("new password for {string} from application {string}")
    public void forgottenPassword(String username, String application) {

        Response response = AuthorizationApiClient.password(username, context.lastAccessToken(), application);

        String token = response.jsonPath().getString("token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @Given("connection with username {string} and password {string} to client {string} with scopes {string}")
    public void connect(String username, String password, String client, String scopes) {

        Response response = AuthorizationApiClient.authorizationByCode(username, password, client, "secret", scopes, context.lastAccessToken());

        String token = response.jsonPath().getString("access_token");

        context.saveAccessToken(token);
        context.save(response);

    }

    @And("connection status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode(), is(status));

    }

    @And("connection error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        assertThat(MAPPER.readTree(context.lastResponse().asString()), is(body));

    }

}
