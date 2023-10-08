package com.exemple.integration.authorization;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.http.Cookie;
import io.restassured.response.Response;

@Component
@ScenarioScope
public class AuthorizationTestContext {

    private String accessToken;

    private Cookie session;

    private Cookie xsrfToken;

    private Response response;

    public void saveAccessToken(String token) {
        this.accessToken = token;
    }

    public Optional<String> lastAccessToken() {
        return Optional.ofNullable(this.accessToken);
    }

    public void saveSession(Cookie session) {
        this.session = session;
    }

    public Optional<Cookie> lastSession() {
        return Optional.ofNullable(this.session);
    }

    public void saveXsrfToken(Cookie xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public Optional<Cookie> lastXsrfToken() {
        return Optional.ofNullable(this.xsrfToken);
    }

    public void save(Response response) {
        this.response = response;
    }

    public Response lastResponse() {
        return this.response;
    }

}
