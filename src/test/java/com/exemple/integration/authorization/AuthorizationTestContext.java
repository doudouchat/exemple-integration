package com.exemple.integration.authorization;

import java.util.LinkedList;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;

@Component
@ScenarioScope
public class AuthorizationTestContext {

    private final LinkedList<String> accessTokens;

    private final LinkedList<Response> responses;

    public AuthorizationTestContext() {
        this.accessTokens = new LinkedList<>();
        this.responses = new LinkedList<>();
    }

    public void saveAccessToken(String token) {
        this.accessTokens.add(token);
    }

    public String lastAccessToken() {
        return this.accessTokens.getLast();
    }

    public void save(Response response) {
        this.responses.add(response);
    }

    public Response lastResponse() {
        return this.responses.getLast();
    }

}
