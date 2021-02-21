package com.exemple.integration.password.v1;

import static com.exemple.integration.core.InitData.ACCESS_ADMIN_TOKEN;
import static com.exemple.integration.core.InitData.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.InitData.ADMIN_APP;
import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.TEST_APP;
import static com.exemple.integration.core.InitData.VERSION_HEADER;
import static com.exemple.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class PasswordIT extends AbstractTestNGSpringContextTests {

    static String ACCESS_TOKEN = null;

    private String USERNAME = UUID.randomUUID().toString();

    @BeforeClass
    public void createLogin() {

        Map<String, Object> login = new HashMap<>();
        login.put("username", USERNAME);
        login.put("password", "mdp");
        login.put("id", UUID.randomUUID().toString());

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(login).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test
    public void password() {

        Map<String, Object> newPassword = new HashMap<>();
        newPassword.put("login", USERNAME);

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, ADMIN_APP)

                .header("Authorization", "Bearer " + ACCESS_ADMIN_TOKEN)

                .body(newPassword).post("/ws/v1/new_password");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

    }

    private String newPassword = UUID.randomUUID().toString();

    @Test(dependsOnMethods = "password")
    public void updateLogin() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", newPassword);

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, ADMIN_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs).patch(LoginIT.URL + "/{login}", USERNAME);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test
    public void readLogin() {

        Map<String, Object> newPassword = new HashMap<>();
        newPassword.put("login", USERNAME);

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, ADMIN_APP)

                .header("Authorization", "Bearer " + ACCESS_ADMIN_TOKEN)

                .body(newPassword).post("/ws/v1/new_password");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        String accessToken = response.jsonPath().getString("token");
        assertThat(accessToken, is(notNullValue()));

        response = JsonRestTemplate.given()

                .header(APP_HEADER, ADMIN_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + accessToken).get(LoginIT.URL + "/{login}", USERNAME);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("username"), is(USERNAME));

    }

    @Test(dependsOnMethods = "updateLogin")
    public void connexionSuccess() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", USERNAME);
        params.put("password", newPassword);
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth()
                .basic("test_user", "secret").formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("access_token"), is(notNullValue()));

    }

    @Test(dependsOnMethods = "updateLogin")
    public void updateLoginForbidden() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", newPassword);

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, ADMIN_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs).patch(LoginIT.URL + "/{login}", USERNAME);

        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED.value()));

    }

}
