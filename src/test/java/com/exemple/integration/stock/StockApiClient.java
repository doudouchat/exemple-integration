package com.exemple.integration.stock;

import static com.exemple.integration.core.InitData.APP_HEADER;

import com.exemple.integration.JsonRestTemplate;

import io.restassured.response.Response;

public final class StockApiClient {

    public static final String STOCK_URL = "/ws/v1/stocks/{store}/{product}";

    private StockApiClient() {

    }

    public static Response post(String store, String product, Object body, String token, String application) {

        return JsonRestTemplate.given().body(body)

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .post(STOCK_URL, store, product);

    }

    public static Response get(String store, String product, String token, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .header("Authorization", "Bearer " + token)

                .get(STOCK_URL, store, product);

    }

}
