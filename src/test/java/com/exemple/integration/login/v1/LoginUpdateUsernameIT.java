package com.exemple.integration.login.v1;

import static com.exemple.integration.core.InitData.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.TEST_APP;
import static com.exemple.integration.core.InitData.VERSION_HEADER;
import static com.exemple.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class LoginUpdateUsernameIT extends AbstractTestNGSpringContextTests {

    private static final String LOGIN_1;

    private static final String LOGIN_2;

    private static String ACCESS_TOKEN = null;

    static {

        UUID random = UUID.randomUUID();

        LOGIN_1 = "1_" + random.toString() + "@gmail.com";

        LOGIN_2 = "2_" + random.toString() + "@gmail.com";
    }

    private static final String NEW_LOGIN = UUID.randomUUID() + "@gmail.com";

    private static final UUID ID = UUID.randomUUID();

    @Test
    public void update() {

        // create login

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN_1);
        body.put("password", "mdp");
        body.put("id", ID);

        Response create = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginIT.URL);

        assertThat(create.getStatusCode(), is(HttpStatus.CREATED.value()));

        // create other login

        body = new HashMap<>();
        body.put("username", LOGIN_2);
        body.put("password", "mdp");
        body.put("id", ID);

        create = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginIT.URL);

        assertThat(create.getStatusCode(), is(HttpStatus.CREATED.value()));

        // connection

        Map<String, Object> connectionParam = new HashMap<>();
        connectionParam.put("grant_type", "password");
        connectionParam.put("username", LOGIN_1);
        connectionParam.put("password", "mdp");
        connectionParam.put("client_id", "test_user");
        connectionParam.put("redirect_uri", "xxx");

        Response connection = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth()
                .basic("test_user", "secret").formParams(connectionParam).post("/oauth/token");

        assertThat(connection.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = connection.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

        // update

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "replace");
        patch1.put("path", "/0/username");
        patch1.put("value", NEW_LOGIN);

        patchs.add(patch1);

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "add");
        patch2.put("path", "/0/disabled");
        patch2.put("value", false);

        patchs.add(patch2);

        Map<String, Object> patch3 = new HashMap<>();
        patch3.put("op", "replace");
        patch3.put("path", "/1/password");
        patch3.put("value", "mdp123");

        patchs.add(patch3);

        Map<String, Object> patch4 = new HashMap<>();
        patch4.put("op", "add");
        patch4.put("path", "/1/disabled");
        patch4.put("value", false);

        patchs.add(patch4);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .body(patchs).patch(LoginIT.URL + "/id/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

        // new connection

        connectionParam.put("username", NEW_LOGIN);

        connection = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth().basic("test_user", "secret")
                .formParams(connectionParam).post("/oauth/token");

        assertThat(connection.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = connection.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

    }

    @DataProvider(name = "logins")
    private static Object[][] logins() {

        return new Object[][] {

                { NEW_LOGIN, "mdp" },

                { LOGIN_2, "mdp123" } };
    }

    @Test(dataProvider = "logins", dependsOnMethods = "update")
    public void getLoginSuccess(String login, String expectedPassword) {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", login);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), is(nullValue()));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(login));
        assertThat(response.jsonPath().getString("disabled"), is("false"));

    }

    @Test(dependsOnMethods = "update")
    public void getLoginFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .get(LoginIT.URL + "/{login}", LOGIN_1);
        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

    @Test(dependsOnMethods = "update")
    public void updateFailure() {

        // update

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "add");
        patch1.put("path", "/0/username");
        patch1.put("value", "jean.dupond@gmail.com");

        patchs.add(patch1);

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "add");
        patch2.put("path", "/0/password");
        patch2.put("value", "mdp");

        patchs.add(patch2);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .body(patchs).patch(LoginIT.URL + "/id/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.jsonPath().getList("code"), contains(is("username")));
        assertThat(response.jsonPath().getList("path"), contains(is("/username")));

    }

}
