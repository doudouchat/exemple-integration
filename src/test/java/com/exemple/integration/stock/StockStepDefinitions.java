package com.exemple.integration.stock;

import static com.exemple.integration.core.InitData.BACK_APP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.integration.authorization.AuthorizationTestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class StockStepDefinitions {

    @Autowired
    private StockTestContext context;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Autowired
    private KafkaConsumer<String, JsonNode> consumerEvent;

    private UUID salt;

    @Before
    public void initKeyspace() {

        salt = UUID.randomUUID();

    }

    @When("increase of {long} for product {string} from store {string}")
    public void increment(long increment, String product, String store) {

        Response response = StockApiClient.increment(store + "#" + salt, product, increment, authorizationContext, BACK_APP);

        context.savePost(response);

    }

    @When("get stock of product {string} from store {string}")
    public void getLogin(String product, String store) {

        Response response = StockApiClient.get(store + "#" + salt, product, authorizationContext, BACK_APP);

        context.saveGet(response);

    }

    @Then("stock of product {string} from store {string} is {long}")
    public void check(String product, String store, long amount) {

        Response response = StockApiClient.get(store + "#" + salt, product, authorizationContext, BACK_APP);

        assertAll(
                () -> assertThat(context.lastResponse().getStatusCode()).as("stock %s %s not found", product, store).isEqualTo(204),
                () -> assertThat(response.jsonPath().getLong("amount")).as("stock %s %s has bad ammount", product, store).isEqualTo(amount));

    }

    @Then("stock of product {string} from store {string} is unknown")
    public void checkUnknown(String product, String store) {

        assertThat(context.lastResponse().getStatusCode()).as("stock %s %s exists", product, store).isEqualTo(404);

    }

    @Then("stock of product {string} from store {string} is {long}, is insufficient for {long}")
    public void checkError(String product, String store, long stock, long quantity) {

        assertAll(
                () -> assertThat(context.lastResponse().getStatusCode()).as("stock %s %s is correct", product, store).isEqualTo(400),
                () -> assertThat(context.lastResponse().getBody().asString())
                        .isEqualTo("Stock " + product + " in " + store + "#" + salt + ":" + stock + " is insufficient for quantity "
                                + quantity));

    }

    @And("stock event is")
    public void getStockEvent(JsonNode body) {

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            ConsumerRecords<String, JsonNode> records = consumerEvent.poll(Duration.ofSeconds(5));
            assertThat(records.iterator()).toIterable().last().satisfies(consumerRecord -> {

                ObjectNode expectedBody = (ObjectNode) consumerRecord.value();
                expectedBody.remove("store");

                assertThat(expectedBody).isEqualTo(body);
            });
        });

    }

}
