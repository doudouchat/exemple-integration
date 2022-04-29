package com.exemple.integration.account;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.VERSION_HEADER;

import com.exemple.integration.JsonRestTemplate;

import io.restassured.response.Response;

public final class AccountApiClient {

    public static final String ACCOUNT_URL = "/ws/v1/accounts";

    private AccountApiClient() {

    }

    public static Response post(Object body, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(body).post(ACCOUNT_URL);

    }

    public static Response get(Object id, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .get(ACCOUNT_URL + "/{id}", id);

    }

    public static Response patch(Object id, Object patchs, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(patchs).patch(ACCOUNT_URL + "/{id}", id);

    }

    public static Response put(Object id, Object patchs, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(patchs).put(ACCOUNT_URL + "/{id}", id);

    }

}
