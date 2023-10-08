package com.exemple.integration.subscription;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.VERSION_HEADER;

import java.util.Collections;

import com.exemple.integration.JsonRestTemplate;
import com.exemple.integration.authorization.AuthorizationTestContext;

import io.restassured.response.Response;

public final class SubscriptionApiClient {

    public static final String SUBSCRIPTION_URL = "/ws/v1/subscriptions";

    private SubscriptionApiClient() {

    }

    public static Response put(String email, Object body, AuthorizationTestContext authorizationContext, String application, String version) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application).header(VERSION_HEADER, version)
                .body(Collections.emptyMap()).put(SUBSCRIPTION_URL + "/{email}", email);

    }

    public static Response get(String email, AuthorizationTestContext authorizationContext, String application, String version) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application).header(VERSION_HEADER, version)
                .get(SUBSCRIPTION_URL + "/{email}", email);

    }

}
