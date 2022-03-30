package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.APP_HEADER;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public final class LoginApiClient {

    public static final String LOGIN_URL = "/ws/v1/logins";

    private LoginApiClient() {

    }

    public static Response get(String username, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .get(LOGIN_URL + "/{username}", username);

    }

    public static Response head(Object login, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .head(LOGIN_URL + "/{login}", login);

    }

    public static Response put(String username, Object body, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .body(body).put(LOGIN_URL + "/{username}", username);

    }

    public static Response delete(Object login, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .delete(LOGIN_URL + "/{login}", login);

    }

    public static Response copy(Object body, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .body(body).post(LOGIN_URL + "/copy");

    }

}
