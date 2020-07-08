package com.exemple.integration.login.v1;

import static com.exemple.integration.account.v1.AccountNominalIT.ACCESS_APP_TOKEN;
import static com.exemple.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class LoginMultipleIT {

    public static final String URL = "/ws/v1/logins";

    private static final String LOGIN_1 = UUID.randomUUID() + "@gmail.com";

    private static final String LOGIN_2 = UUID.randomUUID() + "@gmail.com";

    private static final String LOGIN_3 = UUID.randomUUID() + "@gmail.com";

    private static final UUID ID = UUID.randomUUID();

    private static String ACCESS_TOKEN = null;

    @Test(dependsOnMethods = "com.exemple.integration.account.v1.AccountNominalIT.connexion")
    public void createMultiple() {

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN_1);
        body.put("password", "mdp");
        body.put("id", ID);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginMultipleIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        body.put("username", LOGIN_2);

        response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginMultipleIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        body.put("username", LOGIN_3);
        body.put("id", UUID.randomUUID());

        response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginMultipleIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "createMultiple")
    public void connexionSuccess() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", LOGIN_1);
        params.put("password", "mdp");
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth()
                .basic("test_user", "secret").formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

    }

    @Test(dependsOnMethods = "connexionSuccess")
    public void getSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN_2);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().get("password"), is(nullValue()));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(LOGIN_2));

    }

    @Test(dependsOnMethods = "connexionSuccess")
    public void getFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN_3);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }
}
