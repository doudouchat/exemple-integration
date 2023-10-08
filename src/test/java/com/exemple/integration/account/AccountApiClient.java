package com.exemple.integration.account;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.VERSION_HEADER;

import com.exemple.integration.JsonRestTemplate;
import com.exemple.integration.authorization.AuthorizationTestContext;

import io.restassured.response.Response;

public final class AccountApiClient {

    public static final String ACCOUNT_URL = "/ws/v1/accounts";

    private AccountApiClient() {

    }

    public static Response post(Object body, AuthorizationTestContext authorizationContext, String application, String version) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application).header(VERSION_HEADER, version)
                .body(body).post(ACCOUNT_URL);

    }

    public static Response get(Object id, AuthorizationTestContext authorizationContext, String application, String version) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application).header(VERSION_HEADER, version)
                .get(ACCOUNT_URL + "/{id}", id);

    }

    public static Response patch(Object id, Object patchs, AuthorizationTestContext authorizationContext, String application, String version) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application).header(VERSION_HEADER, version)
                .body(patchs).patch(ACCOUNT_URL + "/{id}", id);

    }

    public static Response put(Object id, Object patchs, AuthorizationTestContext authorizationContext, String application, String version) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application).header(VERSION_HEADER, version)
                .body(patchs).put(ACCOUNT_URL + "/{id}", id);

    }

}
