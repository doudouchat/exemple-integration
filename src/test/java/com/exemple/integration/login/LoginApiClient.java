package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.APP_HEADER;

import com.exemple.integration.JsonRestTemplate;

import io.restassured.response.Response;

public final class LoginApiClient {

    public static final String LOGIN_URL = "/ws/v1/logins";

    private LoginApiClient() {

    }

    public static Response get(String username, String token, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .get(LOGIN_URL + "/{username}", username);

    }

    public static Response head(Object login, String token, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .head(LOGIN_URL + "/{login}", login);

    }

}
