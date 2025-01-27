package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.TEST_APP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.exemple.integration.account.AccountTestContext;
import com.exemple.integration.authorization.AuthorizationApiClient;
import com.exemple.integration.authorization.AuthorizationTestContext;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;

public class LoginStepDefinitions {

    @Autowired
    private AccountTestContext context;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Autowired
    private CqlSession session;

    @Given("delete username {string}")
    public void remove(String username) {

        session.execute("delete from test_authorization.login where username = ?", username);
        Row row = session.execute("select id from test_service.account_username where username = ? and field = ?", username, "email").one();
        if (row != null) {
            Object id = row.getObject(0);
            session.execute("delete from test_service.account where id = ?", id);
        }
        session.execute("delete from test_service.account_username where username = ? and field = ?", username, "email");
    }

    @And("get id account {string}")
    public void getLogin(String username) {

        Response response = LoginApiClient.get(username, authorizationContext, TEST_APP);

        assertAll(
                () -> assertThat(response.getStatusCode()).as("login %s not found", username).isEqualTo(200),
                () -> assertThat(response.as(UUID.class)).as("login id and creation id not match", username).isEqualTo(context.lastId()));

        context.saveId(response.as(UUID.class));

    }

    @And("account {string} exists")
    public void checkExists(String username) {

        Response response = LoginApiClient.head(username, authorizationContext, TEST_APP);
        Response responseAuthorization = AuthorizationApiClient.headLogin(username, authorizationContext, TEST_APP);

        assertAll(
                () -> assertThat(response.getStatusCode()).as("login %s not exists", username).isEqualTo(204),
                () -> assertThat(responseAuthorization.getStatusCode()).as("login %s not exists in authorization", username).isEqualTo(204));
    }

    @And("account {string} not exists")
    public void checkNotExists(String username) {

        Response response = LoginApiClient.head(username, authorizationContext, TEST_APP);
        Response responseAuthorization = AuthorizationApiClient.headLogin(username, authorizationContext, TEST_APP);

        assertAll(
                () -> assertThat(response.getStatusCode()).as("login %s exists", username).isEqualTo(404),
                () -> assertThat(responseAuthorization.getStatusCode()).as("login %s exists in authorization", username).isEqualTo(404));

    }

}
