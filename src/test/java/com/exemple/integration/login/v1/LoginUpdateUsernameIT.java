package com.exemple.integration.login.v1;

import static com.exemple.integration.core.IntegrationTestConfiguration.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.IntegrationTestConfiguration.APP_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.TEST_APP;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class LoginUpdateUsernameIT {

    private static final String LOGIN = UUID.randomUUID() + "@gmail.com";

    private static final String NEW_LOGIN = UUID.randomUUID() + "@gmail.com";

    private static final UUID ID = UUID.randomUUID();

    private static String ACCESS_TOKEN = null;

    @Test
    public void update() {

        // create login

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN);
        body.put("password", "mdp");
        body.put("id", ID);

        Response create = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginIT.URL);

        assertThat(create.getStatusCode(), is(HttpStatus.CREATED.value()));

        // connection

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", LOGIN);
        params.put("password", "mdp");
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response connection = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth()
                .basic("test_user", "secret").formParams(params).post("/oauth/token");

        assertThat(connection.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = connection.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

        // update

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "add");
        patch1.put("path", "/password");
        patch1.put("value", "new_mdp");

        patchs.add(patch1);

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "add");
        patch2.put("path", "/username");
        patch2.put("value", NEW_LOGIN);

        patchs.add(patch2);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "update")
    public void getLoginSuccess() {

        // connection

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", NEW_LOGIN);
        params.put("password", "new_mdp");
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response connection = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth()
                .basic("test_user", "secret").formParams(params).post("/oauth/token");

        assertThat(connection.getStatusCode(), is(HttpStatus.OK.value()));

        String accessToken = connection.jsonPath().getString("access_token");
        assertThat(accessToken, is(notNullValue()));

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + accessToken)

                .get(LoginIT.URL + "/{login}", NEW_LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), is(nullValue()));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(NEW_LOGIN));

    }

    @Test(dependsOnMethods = "update")
    public void getLoginFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }

}
