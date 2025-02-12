package com.exemple.integration.authorization;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;

import com.exemple.integration.JsonRestTemplate;
import com.exemple.integration.core.IntegrationTestConfiguration;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public final class AuthorizationApiClient {

    private static final String LOGIN_URL = "/ws/v1/logins";

    private AuthorizationApiClient() {

    }

    public static Response authorizationByCredentials(String client, String secret, List<String> scopes) {

        Map<String, Object> params = Map.of(
                "grant_type", "client_credentials",
                "scope", scopes.stream().collect(Collectors.joining(" ")));

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)
                .header("Authorization", "Basic " + Base64.encodeBase64String((client + ":" + secret).getBytes()))
                .formParams(params)
                .post("/oauth/token");

    }

    public static Response authorizationByCode(String username, String password, String client, String secret, List<String> scopes,
            AuthorizationTestContext authorizationContext) {

        return authorizationByCode(username, password, client, secret, scopes, authorizationContext, null);

    }

    public static Response authorizationByCode(String username, String password, String client, String secret, List<String> scopes,
            AuthorizationTestContext authorizationContext,
            String application) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC);

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        if (application != null) {
            request.header(APP_HEADER, application);
        }

        Response response = request
                .formParams("username", username, "password", password)
                .post("/login");

        assertThat(response.getStatusCode(), is(302));

        String xAuthToken = response.getHeader("X-Auth-Token");
        assertThat(xAuthToken, is(notNullValue()));

        response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)
                .redirects().follow(false)
                .header("X-Auth-Token", xAuthToken)
                .queryParam("response_type", "code")
                .queryParam("client_id", client)
                .queryParam("scope", scopes.stream().collect(Collectors.joining(" ")))
                .queryParam("state", "123")
                .get("/oauth/authorize");

        assertThat(response.getStatusCode(), is(302));

        String location = response.getHeader("Location");
        assertThat(location, is(notNullValue()));

        Matcher locationMatcher = Pattern.compile(".*code=([a-zA-Z0-9\\-_]*)(&state=)?(.*)?", Pattern.DOTALL).matcher(location);
        assertThat(locationMatcher.lookingAt()).as("location %s is unexpected", location).isTrue();

        String code = locationMatcher.group(1);
        String state = locationMatcher.group(3);

        assertThat(state, is("123"));

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", client);
        params.put("redirect_uri", "http://xxx");

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)
                .header("Authorization", "Basic " + Base64.encodeBase64String((client + ":" + secret).getBytes()))
                .formParams(params)
                .post("/oauth/token");

    }

    public static Response login(String username, String password, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC);

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .formParams("username", username, "password", password)
                .post("/login");

    }

    public static Response disconnection(AuthorizationTestContext authorizationContext) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC);

        authorizationContext.lastAccessToken().ifPresent(token -> request.formParams(Map.of("token", token)));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request.post("/oauth/revoke_token");

    }

    public static Response password(String username, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON);

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .body(Map.of("login", username))
                .post("/ws/v1/new_password");

    }

    public static Response getLogin(String username, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON);

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

    public static Response headLogin(Object login, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON);

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

    public static Response putLogin(String username, Object body, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON);

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .body(body)
                .put(LOGIN_URL + "/{username}", username);

    }

    public static Response moveLogin(Object body, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON);

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .body(body)
                .post(LOGIN_URL + "/move");

    }

}
