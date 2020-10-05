package com.exemple.integration.disconnection.v1;

import static com.exemple.integration.account.v1.AccountNominalIT.ACCOUNT_URL;
import static com.exemple.integration.core.IntegrationTestConfiguration.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.IntegrationTestConfiguration.APP_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.TEST_APP;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_V0;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.integration.login.v1.LoginIT;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class DisconnectionIT extends AbstractTestNGSpringContextTests {

    static String ACCESS_TOKEN = null;

    private String USERNAME = UUID.randomUUID().toString() + "@gmail.com";

    private UUID ID;

    @BeforeClass
    public void createAccount() {

        Map<String, Object> account = new HashMap<>();
        account.put("email", USERNAME);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V0)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(account).post(ACCOUNT_URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        ID = UUID.fromString(response.getHeader("Location").substring(response.getHeader("Location").lastIndexOf('/') + 1));

        Map<String, Object> login = new HashMap<>();
        login.put("username", USERNAME);
        login.put("password", "mdp");
        login.put("id", ID);

        response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(login).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test
    public void connection() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", USERNAME);
        params.put("password", "mdp");
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth()
                .basic("test_user", "secret").formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

        response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN).get(ACCOUNT_URL + "/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

    }

    @Test(dependsOnMethods = "connection")
    public void disconnection() {

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, TEST_APP)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .post("/ws/v1/disconnection");

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "disconnection")
    public void getFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN).get(ACCOUNT_URL + "/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));
    }

}
