/*
 * Copyright 2019, Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka.bridge.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.strimzi.kafka.bridge.config.BridgeConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsumerGeneratedNameTest extends HttpBridgeTestBase {

    private String groupId = "my-group";
    private static String bridgeID = "";

    @BeforeAll
    static void unsetBridgeID() {
        bridgeID = config.get(BridgeConfig.BRIDGE_ID).toString();
        config.remove(BridgeConfig.BRIDGE_ID);

        bridgeConfig = BridgeConfig.fromMap(config);
        httpBridge = new HttpBridge(bridgeConfig);
    }

    @AfterAll
    static void revertUnsetBridgeID() {
        config.put(BridgeConfig.BRIDGE_ID, bridgeID);
    }

    @Test
    void createConsumerNameNotSet(VertxTestContext context) throws InterruptedException, ExecutionException, TimeoutException {
        JsonObject json = new JsonObject();
        AtomicReference<String> name = new AtomicReference<>();

        CompletableFuture<Boolean> create = new CompletableFuture<>();
        consumerService()
            .createConsumerRequest(groupId, json)
                .as(BodyCodec.jsonObject())
                .sendJsonObject(json, ar -> {
                    context.verify(() -> {
                        assertTrue(ar.succeeded());
                        HttpResponse<JsonObject> response = ar.result();
                        assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
                        JsonObject bridgeResponse = response.body();
                        String consumerInstanceId = bridgeResponse.getString("instance_id");
                        name.set(consumerInstanceId);
                        assertTrue(consumerInstanceId.startsWith("kafka-bridge-consumer-"));
                        create.complete(true);
                    });
                });
        create.get(TEST_TIMEOUT, TimeUnit.SECONDS);
        consumerService().deleteConsumer(context, groupId, name.get());
        context.completeNow();
    }

    @Test
    void createConsumerNameSet(VertxTestContext context) throws InterruptedException, ExecutionException, TimeoutException {
        JsonObject json = new JsonObject()
                .put("name", "consumer-1")
                .put("format", "json");

        CompletableFuture<Boolean> create = new CompletableFuture<>();
        consumerService()
                .createConsumerRequest(groupId, json)
                .as(BodyCodec.jsonObject())
                .sendJsonObject(json, ar -> {
                    context.verify(() -> {
                        assertTrue(ar.succeeded());
                        HttpResponse<JsonObject> response = ar.result();
                        assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
                        JsonObject bridgeResponse = response.body();
                        String consumerInstanceId = bridgeResponse.getString("instance_id");
                        assertTrue(consumerInstanceId.equals("consumer-1"));
                        create.complete(true);
                    });
                });
        create.get(TEST_TIMEOUT, TimeUnit.SECONDS);
        consumerService().deleteConsumer(context, groupId, "consumer-1");
        context.completeNow();
    }
}
