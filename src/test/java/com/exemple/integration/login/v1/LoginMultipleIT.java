package com.exemple.integration.login.v1;

import static com.exemple.integration.core.IntegrationTestConfiguration.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.IntegrationTestConfiguration.APP_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.TEST_APP;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
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

    private static final UUID ID_3 = UUID.randomUUID();

    private static String ACCESS_TOKEN = null;

    @Test
    public void createMultiple() {

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN_1);
        body.put("password", "mdp");
        body.put("id", ID);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginMultipleIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        body.put("username", LOGIN_2);

        response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginMultipleIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        body.put("username", LOGIN_3);
        body.put("id", ID_3);

        response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

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

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN_2);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().get("password"), is(nullValue()));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(LOGIN_2));

    }

    @Test(dependsOnMethods = "connexionSuccess")
    public void getById() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(URL + "/id/{id}", ID);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().getList("password"), everyItem(is(nullValue())));
        assertThat(response.jsonPath().getList("id"), everyItem(is(ID.toString())));
        assertThat(response.jsonPath().getList("username"), containsInAnyOrder(equalTo(LOGIN_1), equalTo(LOGIN_2)));

    }

    @Test(dependsOnMethods = "connexionSuccess")
    public void getFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN_3);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

    @Test(dependsOnMethods = "connexionSuccess")
    public void getByIdFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/id/{id}", ID_3);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }
}
