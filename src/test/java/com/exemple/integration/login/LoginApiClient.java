package com.exemple.integration.login;

import static com.exemple.integration.core.InitData.APP_HEADER;

import com.exemple.integration.JsonRestTemplate;
import com.exemple.integration.authorization.AuthorizationTestContext;

import io.restassured.response.Response;

public final class LoginApiClient {

    public static final String LOGIN_URL = "/ws/v1/logins";

    private LoginApiClient() {

    }

    public static Response get(String username, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .get(LOGIN_URL + "/{username}", username);

    }

    public static Response head(Object login, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .head(LOGIN_URL + "/{login}", login);

    }

}
