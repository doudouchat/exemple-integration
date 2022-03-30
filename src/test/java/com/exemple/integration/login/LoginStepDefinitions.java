package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.TEST_APP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.integration.account.AccountTestContext;
import com.exemple.integration.authorization.AuthorizationTestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class LoginStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountTestContext context;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Autowired
    private CqlSession session;

    @Given("delete username {string}")
    public void remove(String username) {

        session.execute("delete from test_authorization.login where username = ?", username);
        session.execute("delete from test_service.login where username = ?", username);

    }

    @And("create authorization login {string}")
    public void createLogin(String username, JsonNode body) {

        Response response = LoginApiClient.put(username, body, authorizationContext.lastAccessToken(), TEST_APP);

        context.savePut(response);

    }

    @When("put login {string}")
    public void putLogin(String username, JsonNode body) {

        Response response = LoginApiClient.put(username, body, authorizationContext.lastAccessToken(), TEST_APP);

        context.savePut(response);

    }

    @When("put login {string} for application {string}")
    public void putLogin(String username, String application, JsonNode body) {

        Response response = LoginApiClient.put(username, body, authorizationContext.lastAccessToken(), application);

        context.savePut(response);

    }

    @And("move authorization login from {string} to {string}")
    public void copy(String fromUsername, String toUsername) {

        ObjectNode body = MAPPER.createObjectNode();
        body.put("fromUsername", fromUsername);
        body.put("toUsername", toUsername);

        Response responseCopy = LoginApiClient.copy(body, authorizationContext.lastAccessToken(), TEST_APP);
        LoginApiClient.delete(fromUsername, authorizationContext.lastAccessToken(), TEST_APP);

        context.savePost(responseCopy);

    }

    @When("get id account {string}")
    public void getLogin(String username) {

        Response response = LoginServiceApiClient.get(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(200),
                () -> assertThat(response.as(UUID.class)).isEqualTo(context.lastId()));

        context.saveId(response.as(UUID.class));

    }

    @And("account {string} exists")
    public void checkExists(String username) {

        Response responseServiceApi = LoginServiceApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);
        Response response = LoginApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(204),
                () -> assertThat(responseServiceApi.getStatusCode()).isEqualTo(204));
    }

    @And("account {string} not exists")
    public void checkNotExists(String username) {

        Response responseServiceApi = LoginServiceApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);
        Response response = LoginApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(404),
                () -> assertThat(responseServiceApi.getStatusCode()).isEqualTo(404));

    }

}
