package com.exemple.integration.subscription;

import static com.exemple.integration.core.InitData.TEST_APP;
import static com.exemple.integration.core.InitData.VERSION_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.condition.AnyOf.anyOf;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.assertj.core.api.Condition;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.integration.authorization.AuthorizationTestContext;
import com.exemple.service.customer.subscription.SubscriptionResource;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class SubscriptionStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SubscriptionTestContext context;

    @Autowired
    private KafkaConsumer<String, JsonNode> consumerEvent;

    @Autowired
    private SubscriptionResource subscriptionResource;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Before
    public void initKeyspace() {

        ResourceExecutionContext.get().setKeyspace("test_service");

    }

    @Given("delete subscription {string}")
    public void remove(String email) {

        subscriptionResource.delete(email);

    }

    @When("create subscription {string}")
    public void createSubscription(String email) {

        Response response = SubscriptionApiClient.put(email, Collections.emptyMap(), authorizationContext.lastAccessToken(), TEST_APP, VERSION_V1);

        context.savePut(response);

    }

    @And("subscription {string} is")
    public void getSubscription(String email, JsonNode body) throws IOException {

        assertAll(
                () -> assertThat(context.lastResponse().getStatusCode()).as("subscription %s is not created", email).is(anyOf(
                        new Condition<>(status -> status == 204, "status"),
                        new Condition<>(status -> status == 201, "status"))),
                () -> assertThat(context.lastResponse().asString()).isEmpty());

        Response response = SubscriptionApiClient.get(email, authorizationContext.lastAccessToken(), TEST_APP, VERSION_V1);

        assertThat(response.getStatusCode()).as("subscription %s not found", email).isEqualTo(200);

        ObjectNode expectedBody = (ObjectNode) MAPPER.readTree(response.asString());
        expectedBody.remove("subscription_date");

        assertThat(expectedBody).isEqualTo(body);

        context.saveGet(response);

    }

    @And("subscription event is")
    public void getSubscriptionEvent(JsonNode body) {

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ConsumerRecords<String, JsonNode> records = consumerEvent.poll(Duration.ofSeconds(5));
            assertThat(records.iterator()).toIterable().last().satisfies(record -> {

                ObjectNode expectedBody = (ObjectNode) record.value();
                expectedBody.remove("subscription_date");

                assertThat(expectedBody).isEqualTo(body);
            });
        });

    }

    @And("subscription {string} is unknown")
    public void getSubscription(String email) throws IOException {

        Response response = SubscriptionApiClient.get(email, authorizationContext.lastAccessToken(), TEST_APP, VERSION_V1);

        assertThat(response.getStatusCode()).as("subscription %s exists", email).isEqualTo(404);

    }

    @And("subscription contains {string}")
    public void checkProperty(String property) {

        assertThat(context.lastGet().jsonPath().getString(property)).as("subscription property %s exists", property).isNotNull();

    }

    @And("subscription error only contains")
    public void checkOnlyError(JsonNode body) throws IOException {

        checkCountError(1);
        checkErrors(body);
    }

    @And("subscription error contains {int} errors")
    public void checkCountError(int count) throws IOException {

        assertThat(context.lastResponse().getStatusCode()).as("subscription is correct").isEqualTo(400);

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());

        assertThat(Streams.stream(errors.elements())).as("errors %s not contain expected errors", errors.toPrettyString()).hasSize(count);

    }

    @And("subscription error contains")
    public void checkErrors(JsonNode body) throws IOException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());
        assertAll(
                () -> assertThat(context.lastResponse().getStatusCode()).isEqualTo(400),
                () -> assertThat(errors).as("errors {} not contain {}", errors.toPrettyString(), body.toPrettyString())
                        .anySatisfy(error -> {
                            Iterator<Map.Entry<String, JsonNode>> expectedErrors = body.fields();
                            while (expectedErrors.hasNext()) {
                                Map.Entry<String, JsonNode> expectedError = expectedErrors.next();
                                assertThat(error.get(expectedError.getKey())).isEqualTo(expectedError.getValue());
                            }
                        }));
    }
}
