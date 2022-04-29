package com.exemple.integration.subscription;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.VERSION_HEADER;

import java.util.Collections;

import com.exemple.integration.JsonRestTemplate;

import io.restassured.response.Response;

public final class SubscriptionApiClient {

    public static final String SUBSCRIPTION_URL = "/ws/v1/subscriptions";

    private SubscriptionApiClient() {

    }

    public static Response put(String email, Object body, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .body(Collections.emptyMap()).put(SUBSCRIPTION_URL + "/{email}", email);

    }

    public static Response get(String email, String token, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .header("Authorization", "Bearer " + token)

                .get(SUBSCRIPTION_URL + "/{email}", email);

    }

}
