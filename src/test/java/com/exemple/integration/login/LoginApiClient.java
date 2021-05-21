package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.VERSION_HEADER;

import java.util.UUID;

import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public final class LoginApiClient {

    public static final String LOGIN_URL = "/ws/v1/logins";

    private LoginApiClient() {

    }

    public static Response post(Object body, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(body).post(LOGIN_URL);

    }

    public static Response get(String username, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .get(LOGIN_URL + "/{username}", username);

    }

    public static Response get(UUID id, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .get(LOGIN_URL + "/id/{id}", id);

    }

    public static Response head(Object login, String token, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .head(LOGIN_URL + "/{login}", login);

    }

    public static Response delete(Object login, String token, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .delete(LOGIN_URL + "/{login}", login);

    }

    public static Response patch(String login, Object patchs, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(patchs).patch(LOGIN_URL + "/{login}", login);

    }

    public static Response patch(UUID id, Object patchs, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(patchs).patch(LOGIN_URL + "/id/{id}", id);

    }

    public static Response put(Object login, Object patchs, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(patchs).put(LOGIN_URL + "/{login}", login);

    }

}
