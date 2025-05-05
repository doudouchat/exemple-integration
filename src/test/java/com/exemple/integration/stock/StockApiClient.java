package com.exemple.integration.stock;

import static com.exemple.integration.core.InitData.APP_HEADER;

import com.exemple.integration.JsonRestTemplate;
import com.exemple.integration.authorization.AuthorizationTestContext;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public final class StockApiClient {

    public static final String STOCK_URL = "/ws/v1/stocks/{store}/{product}";

    private StockApiClient() {

    }

    public static Response increment(String store, String product, Long body, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given(ContentType.TEXT);

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .body(body)
                .post(STOCK_URL + "/_increment", store, product);

    }

    public static Response get(String store, String product, AuthorizationTestContext authorizationContext, String application) {

        var request = JsonRestTemplate.given();

        authorizationContext.lastAccessToken().ifPresent(token -> request.header("Authorization", "Bearer " + token));
        authorizationContext.lastSession().ifPresent(session -> request.cookie("JSESSIONID", session.getValue()));
        authorizationContext.lastXsrfToken().ifPresent(token -> {
            request.header("X-XSRF-TOKEN", token.getValue());
            request.cookie("XSRF-TOKEN", token.getValue());
        });

        return request
                .header(APP_HEADER, application)
                .get(STOCK_URL, store, product);

    }

}
