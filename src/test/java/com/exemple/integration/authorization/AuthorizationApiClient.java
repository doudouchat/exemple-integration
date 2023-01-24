package com.exemple.integration.authorization;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.charset.StandardCharsets;
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
import io.restassured.specification.RequestSpecification;

public final class AuthorizationApiClient {

    private static final String LOGIN_URL = "/ws/v1/logins";

    private AuthorizationApiClient() {

    }

    public static Response authorizationByCredentials(String client, String secret, List<String> scopes) {

        Map<String, Object> params = Map.of(
                "grant_type", "client_credentials",
                "scope", scopes.stream().collect(Collectors.joining(" ")));

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)
                .header("Authorization", "Basic " + Base64.encodeBase64String((client + ":" + secret).getBytes(StandardCharsets.UTF_8)))
                .formParams(params)
                .post("/oauth/token");

    }

    public static Response authorizationByCode(String username, String password, String client, String secret, List<String> scopes, String token) {

        return authorizationByCode(username, password, client, secret, scopes, token, null);

    }

    public static Response authorizationByCode(String username, String password, String client, String secret, List<String> scopes, String token,
            String application) {

        RequestSpecification requestSpecification = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)
                .header("Authorization", "Bearer " + token)
                .formParams("username", username, "password", password);

        if (application != null) {
            requestSpecification.header(APP_HEADER, application);
        }

        Response response = requestSpecification.post("/login");

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
                .header("Authorization", "Basic " + Base64.encodeBase64String((client + ":" + secret).getBytes(StandardCharsets.UTF_8)))
                .formParams(params)
                .post("/oauth/token");

    }

    public static Response login(String username, String password, String token, String application) {

        RequestSpecification requestSpecification = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)
                .header("Authorization", "Bearer " + token)
                .header(APP_HEADER, application)
                .formParams("username", username, "password", password);

        return requestSpecification.post("/login");

    }

    public static Response disconnection(String token, String application) {

        Map<String, String> params = Map.of("token", token);

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)
                .formParams(params)
                .post("/oauth/revoke_token");

    }

    public static Response password(String username, String token, String application) {

        Map<String, Object> newPassword = new HashMap<>();
        newPassword.put("login", username);

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)
                .header(APP_HEADER, application)
                .header("Authorization", "Bearer " + token)
                .body(newPassword)
                .post("/ws/v1/new_password");

    }

    public static Response getLogin(String username, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)
                .header(APP_HEADER, application)
                .header("Authorization", "Bearer " + token)
                .get(LOGIN_URL + "/{username}", username);

    }

    public static Response headLogin(Object login, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)
                .header(APP_HEADER, application)
                .header("Authorization", "Bearer " + token)
                .head(LOGIN_URL + "/{login}", login);

    }

    public static Response putLogin(String username, Object body, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)
                .header(APP_HEADER, application)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .put(LOGIN_URL + "/{username}", username);

    }

    public static Response moveLogin(Object body, String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)
                .header(APP_HEADER, application)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .post(LOGIN_URL + "/move");

    }

}
