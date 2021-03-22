package com.exemple.integration.login.v1;

import static com.exemple.integration.core.InitData.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.TEST_APP;
import static com.exemple.integration.core.InitData.VERSION_HEADER;
import static com.exemple.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class LoginUpdatePasswordIT extends AbstractTestNGSpringContextTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String LOGIN = UUID.randomUUID() + "@gmail.com";

    private static final UUID ID = UUID.randomUUID();

    private static final String PASSWORD = "mpd123";

    private static String ACCESS_TOKEN = null;

    @Test
    public void update() throws IOException {

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

        // read
        Response read = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN);

        @SuppressWarnings("unchecked")
        Map<String, Object> model = MAPPER.convertValue(MAPPER.readTree(read.getBody().asString()), Map.class);

        // update
        model.put("password", PASSWORD);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .body(model).put(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "update")
    public void updateFailure() throws IOException {

        // read
        Response read = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN);

        @SuppressWarnings("unchecked")
        Map<String, Object> model = MAPPER.convertValue(MAPPER.readTree(read.getBody().asString()), Map.class);

        // update
        model.remove("id");
        model.put("password", PASSWORD);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .body(model).put(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.jsonPath().getList("code"), everyItem(is("required")));
        assertThat(response.jsonPath().getList("path"), everyItem(is("/id")));

    }

    @Test(dependsOnMethods = "update")
    public void getLoginSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), is(nullValue()));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(LOGIN));
        assertThat(response.jsonPath().getString("disable"), is(nullValue()));

    }
}
