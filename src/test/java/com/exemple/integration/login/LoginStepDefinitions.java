package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.TEST_APP;
import static com.exemple.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.integration.account.AccountTestContext;
import com.exemple.integration.authorization.AuthorizationTestContext;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.login.LoginResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Streams;

import io.cucumber.java.Before;
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
    private AccountTestContext accountContext;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Autowired
    private LoginResource loginResource;

    @Before
    public void initKeyspace() {

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test_keyspace");

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

    }

    @Given("delete username {string}")
    public void remove(String username) {

        loginResource.delete(username);

    }

    @When("create login for application {string} and version {string}")
    public void createLogin(String application, String version, JsonNode body) {

        Response response = LoginApiClient.post(body, authorizationContext.lastAccessToken(), application, version);

        context.savePost(response);

    }

    @When("create login for application {string} and version {string} and last id")
    public void createLoginWithLastId(String application, String version, JsonNode body) {

        ((ObjectNode) body).set("id", TextNode.valueOf(accountContext.lastId().toString()));

        Response response = LoginApiClient.post(body, authorizationContext.lastAccessToken(), application, version);

        context.savePost(response);

    }

    @When("delete login {string}")
    public void deleteLogin(String username) {

        Response response = LoginApiClient.delete(username, authorizationContext.lastAccessToken(), TEST_APP);

        context.saveDelete(response);

    }

    @When("patch login {string} for application {string} and version {string}")
    public void patchLogin(String username, String application, String version, JsonNode body) {

        Response response = LoginApiClient.patch(username, body, authorizationContext.lastAccessToken(), application, version);

        context.savePatch(response);

    }

    @When("patch login by id {id} for application {string} and version {string}")
    public void patchLogin(UUID id, String application, String version, JsonNode body) {

        Response response = LoginApiClient.patch(id, body, authorizationContext.lastAccessToken(), application, version);

        context.savePatch(response);

    }

    @When("put login {string} for application {string} and version {string}")
    public void putLogin(String username, String application, String version, JsonNode body) {

        Response response = LoginApiClient.put(username, body, authorizationContext.lastAccessToken(), application, version);

        context.savePut(response);

    }

    @When("get login {string} for application {string} and version {string}")
    public void getLogin(String username, String application, String version) {

        Response response = LoginApiClient.get(username, authorizationContext.lastAccessToken(), application, version);

        context.saveGet(response);

    }

    @When("get login by id {id} for application {string} and version {string}")
    public void getLogin(UUID id, String application, String version) {

        Response response = LoginApiClient.get(id, authorizationContext.lastAccessToken(), application, version);

        context.saveGet(response);

    }

    @Then("login status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode(), is(status));

    }

    @And("login {string} exists")
    public void checkExists(String username) {

        Response response = LoginApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertThat(response.getStatusCode(), is(204));

    }

    @And("login by id {id} exists")
    public void checkExists(UUID id) {

        getLogin(id, TEST_APP, VERSION_V1);

        checkStatus(200);

    }

    @And("login {string} not exists")
    public void checkNotExists(String username) {

        Response response = LoginApiClient.head(username, authorizationContext.lastAccessToken(), TEST_APP);

        assertThat(response.getStatusCode(), is(404));

        getLogin(username, TEST_APP, VERSION_V1);

        checkStatus(404);

    }

    @And("login {string} is")
    public void checkBody(String username, JsonNode body) throws JsonProcessingException {

        getLogin(username, TEST_APP, VERSION_V1);

        checkStatus(200);

        assertThat(MAPPER.readTree(context.lastGet().asString()), is(body));

    }

    @And("logins {id} are")
    public void checkLogins(UUID id, JsonNode body) throws JsonProcessingException {

        getLogin(id, TEST_APP, VERSION_V1);

        checkStatus(200);

        assertThat(MAPPER.readTree(context.lastGet().asString()), is(body));

    }

    @And("login error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());

        assertThat(errors, is(body));

    }

    @And("login error is expect {string}")
    public void checkError(String expect, JsonNode body) throws JsonProcessingException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());
        Streams.stream(errors.elements()).map(ObjectNode.class::cast).forEach((ObjectNode error) -> error.remove(expect));

        assertThat(errors, is(body));

    }

}
