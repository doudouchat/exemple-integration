package com.exemple.integration.authorization;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public final class AuthorizationApiClient {

    private AuthorizationApiClient() {

    }

    public static Response authorizationByCredentials(String client, String secret) {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credentials");

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth().basic(client, secret)
                .formParams(params).post("/oauth/token");

    }

    public static Response authorizationByPassword(String username, String password, String client, String secret) {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", username);
        params.put("password", password);
        params.put("client_id", client);
        params.put("redirect_uri", "xxx");

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth().basic(client, secret)
                .formParams(params).post("/oauth/token");

    }

    public static Response authorizationByCode(String username, String password, String client, String secret, String scopes, String token) {

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)

                .header("Authorization", "Bearer " + token).formParams("username", username, "password", password)

                .post("/login");

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND.value()));

        String xAuthToken = response.getHeader("X-Auth-Token");
        assertThat(xAuthToken, is(notNullValue()));

        response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).redirects().follow(false)

                .header("X-Auth-Token", xAuthToken)

                .queryParam("response_type", "code")

                .queryParam("client_id", client)

                .queryParam("scope", scopes)

                .queryParam("state", "123")

                .get("/oauth/authorize");

        assertThat(response.getStatusCode(), is(HttpStatus.SEE_OTHER.value()));

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertThat(location, is(notNullValue()));

        Matcher locationMatcher = Pattern.compile(".*code=(\\w*)(&state=)?(.*)?", Pattern.DOTALL).matcher(location);
        assertThat(locationMatcher.lookingAt(), is(true));

        String code = locationMatcher.group(1);
        String state = locationMatcher.group(3);

        assertThat(state, is("123"));

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", client);
        params.put("redirect_uri", "xxx");

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC)

                .auth().basic(client, secret)

                .formParams(params).post("/oauth/token");

    }

    public static Response disconnection(String token, String application) {

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .post("/ws/v1/disconnection");

    }

    public static Response password(String username, String token, String application) {

        Map<String, Object> newPassword = new HashMap<>();
        newPassword.put("login", username);

        return JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .body(newPassword).post("/ws/v1/new_password");

    }

}