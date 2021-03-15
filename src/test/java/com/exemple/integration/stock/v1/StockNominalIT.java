package com.exemple.integration.stock.v1;

import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.BACK_APP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class StockNominalIT extends AbstractTestNGSpringContextTests {

    private static final String STOCK_URL = "/ws/v1/stocks/{store}/{product}";

    private final String product = "product#" + UUID.randomUUID();

    private final String store = "store#" + UUID.randomUUID();

    public static String ACCESS_TOKEN = null;

    @Test
    public void connexionBack() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", "admin");
        params.put("password", "admin123");
        params.put("client_id", "back_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(IntegrationTestConfiguration.AUTHORIZATION_URL, ContentType.URLENC).auth()
                .basic("back_user", "secret").formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

    }

    @DataProvider(name = "updateSuccess")
    private static Object[][] updateSuccess() {

        return new Object[][] {

                { 5 },

                { 8 }

        };
    }

    @Test(dataProvider = "updateSuccess", dependsOnMethods = "connexionBack")
    public void updateSuccess(int increment) {

        Response response = JsonRestTemplate.given().body(Collections.singletonMap("increment", increment))

                .header("Authorization", "Bearer " + ACCESS_TOKEN).header(APP_HEADER, BACK_APP)

                .post(STOCK_URL, store, product);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test(dependsOnMethods = "updateSuccess")
    public void getSuccess() {

        Response response = JsonRestTemplate.given()

                .header("Authorization", "Bearer " + ACCESS_TOKEN).header(APP_HEADER, BACK_APP)

                .get(STOCK_URL, store, product);

        assertThat(response.jsonPath().getInt("amount"), is(13));

    }

    @Test(dependsOnMethods = "getSuccess")
    public void getFailure() {

        Response response = JsonRestTemplate.given()

                .header("Authorization", "Bearer " + ACCESS_TOKEN).header(APP_HEADER, BACK_APP)

                .get(STOCK_URL, store, "product#" + UUID.randomUUID());

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }

    @Test(dependsOnMethods = "getSuccess")
    public void updateFailure() {

        Response response = JsonRestTemplate.given().body(Collections.singletonMap("increment", -100))

                .header("Authorization", "Bearer " + ACCESS_TOKEN).header(APP_HEADER, BACK_APP)

                .post(STOCK_URL, store, product);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getBody().asString(), is("Stock /test_company/" + store + "/" + product + ":13 is insufficient for quantity -100"));

    }

}
