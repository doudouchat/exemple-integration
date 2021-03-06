package com.exemple.integration.subscription.v1;

import static com.exemple.integration.core.InitData.ACCESS_APP_TOKEN;
import static com.exemple.integration.core.InitData.APP_HEADER;
import static com.exemple.integration.core.InitData.TEST_APP;
import static com.exemple.integration.core.InitData.VERSION_HEADER;
import static com.exemple.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.exemple.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class SubscriptionIT {

    public static final String URL = "/ws/v1/subscriptions";

    private static final String EMAIL = UUID.randomUUID().toString() + "@gmail.com";

    @Test
    public void createSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(Collections.emptyMap()).put(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "createSubscription")
    public void updateSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(Collections.emptyMap()).put(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "createSubscription")
    public void readSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN).get(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("email"), is(EMAIL));
        assertThat(response.jsonPath().getString("subscription_date"), is(notNullValue()));

    }

    @Test
    public void createSubscriptionFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(Collections.emptyMap()).put(URL + "/{email}", "toto");

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.jsonPath().getList("code").get(0), is("format"));
        assertThat(response.jsonPath().getList("path").get(0), is("/email"));

    }

    @Test
    public void readSubscriptionFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .get(URL + "/{email}", UUID.randomUUID().toString() + "@gmail.com");

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));
    }
}
