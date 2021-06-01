package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.TEST_APP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.integration.account.AccountTestContext;
import com.exemple.integration.authorization.AuthorizationTestContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class LoginStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private LoginTestContext context;

    @Autowired
    private LoginServiceTestContext loginContext;;

    @Autowired
    private AccountTestContext accountContext;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Autowired
    private CqlSession session;

    @Given("delete username {string}")
    public void remove(String username) {

        session.execute("delete from test_authorization.login where username = ?", username);
        session.execute("delete from test_service.login where username = ?", username);

    }

    @When("create authorization login {string} for application {string}")
    public void createLogin(String username, String application, JsonNode body) {

        Response response = LoginApiClient.put(username, body, authorizationContext.lastAccessToken(), application);

        context.savePut(response);

    }

    @When("create service login for application {string} and last id")
    public void createLoginWithLastId(String application, JsonNode body) {

        ((ObjectNode) body).set("id", TextNode.valueOf(accountContext.lastId().toString()));

        Response response = LoginServiceApiClient.post(body, authorizationContext.lastAccessToken(), application);

        loginContext.savePost(response);

    }

    @When("create service login for application {string}")
    public void createLoginWithId(String application, JsonNode body) {

        Response response = LoginServiceApiClient.post(body, authorizationContext.lastAccessToken(), application);

        loginContext.savePost(response);

    }

    @When("delete login {string}")
    public void deleteLogin(String username) {

        Response response = LoginApiClient.delete(username, authorizationContext.lastAccessToken(), TEST_APP);

        context.saveDelete(response);

        response = LoginServiceApiClient.delete(username, authorizationContext.lastAccessToken(), TEST_APP);

        loginContext.saveDelete(response);

    }

    @When("get authorization login {string} for application {string}")
    public void getLogin(String username, String application) {

        Response response = LoginApiClient.get(username, authorizationContext.lastAccessToken(), application);

        context.saveGet(response);

    }

    @When("get service login {string} for application {string}")
    public void getServiceLogin(String username, String application) {

        Response response = LoginServiceApiClient.get(username, authorizationContext.lastAccessToken(), application);

        loginContext.saveGet(response);

    }

    @When("put login {string} for application {string}")
    public void putLogin(String username, String application, JsonNode body) {

        Response response = LoginApiClient.put(username, body, authorizationContext.lastAccessToken(), application);

        context.savePut(response);

    }

    @When("copy authorization login for application {string}")
    public void copy(String application, JsonNode body) {

        Response response = LoginApiClient.copy(body, authorizationContext.lastAccessToken(), application);

        context.savePost(response);

    }

    @Then("login authorization status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode(), is(status));

    }

    @Then("login service status is {int}")
    public void checkServiceStatus(int status) {

        assertThat(loginContext.lastResponse().getStatusCode(), is(status));

    }

    @And("login authorization {string} exists")
    public void checkExists(String username) {

        Response response = LoginApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertThat(response.getStatusCode(), is(204));

    }

    @And("login service {string} exists")
    public void checkLoginExists(String username) {

        Response response = LoginServiceApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertThat(response.getStatusCode(), is(204));

    }

    @And("login authorization {string} not exists")
    public void checkNotExists(String username) {

        Response response = LoginApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertThat(response.getStatusCode(), is(404));

        getLogin(username, TEST_APP);

        checkStatus(404);

    }

    @And("login service {string} not exists")
    public void checkLoginNotExists(String username) {

        Response response = LoginServiceApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertThat(response.getStatusCode(), is(404));

        getServiceLogin(username, TEST_APP);

        checkServiceStatus(404);

    }

    @And("login authorization {string} is")
    public void checkBody(String username, JsonNode body) throws JsonProcessingException {

        getLogin(username, TEST_APP);

        checkStatus(200);

        assertThat(MAPPER.readTree(context.lastGet().asString()), is(body));

    }

    @And("login service {string} is")
    public void checkLoginBody(String username, JsonNode body) throws JsonProcessingException {

        getServiceLogin(username, TEST_APP);

        checkServiceStatus(200);

        assertThat(MAPPER.readTree(loginContext.lastGet().asString()), is(body));

    }

    @And("login authorization error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        JsonNode errors = MAPPER.readTree(context.lastResponse().asString());

        assertThat(errors, is(body));

    }

    @And("login service error is")
    public void checkLoginError(JsonNode body) throws JsonProcessingException {

        JsonNode errors = MAPPER.readTree(loginContext.lastResponse().asString());

        assertThat(errors, is(body));

    }

}
