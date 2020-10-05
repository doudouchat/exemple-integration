package com.exemple.integration.login.v1;

import static com.exemple.integration.core.IntegrationTestConfiguration.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.IntegrationTestConfiguration.APP_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.TEST_APP;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_HEADER;
import static com.exemple.integration.core.IntegrationTestConfiguration.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class LoginIT {

    public static final String URL = "/ws/v1/logins";

    private static final String LOGIN = UUID.randomUUID() + "@gmail.com";

    private static String ACCESS_TOKEN = null;

    @Test
    public void create() {

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN);
        body.put("password", "mdp");
        body.put("id", UUID.randomUUID());

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "create")
    public void exist() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .head(URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "exist")
    public void connexionSuccess() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", LOGIN);
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
    public void update() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", "new_mdp");

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @DataProvider(name = "updateFailure")
    private static Object[][] updateFailure() {

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "replace");
        patch0.put("path", "/id");
        patch0.put("value", UUID.randomUUID());

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "replace");
        patch1.put("path", "/username");
        patch1.put("value", "jean.dupond@gmail.com");

        return new Object[][] {
                // id is createOnly
                { patch0, "/id", "readOnly" },
                // username is unique
                { patch1, "/username", "login" } };
    }

    @Test(dataProvider = "updateFailure", dependsOnMethods = "connexionSuccess")
    public void updateFailure(Map<String, Object> patch, String expectedPath, String expectedCode) {

        List<Map<String, Object>> patchs = Collections.singletonList(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.jsonPath().getList("code").get(0), is(expectedCode));
        assertThat(response.jsonPath().getList("path").get(0), is(expectedPath));

    }

    @Test(dependsOnMethods = { "update", "updateFailure" })
    public void delete() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header("Authorization", "Bearer " + ACCESS_TOKEN)

                .delete(URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "delete")
    public void notFound() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .head(URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
