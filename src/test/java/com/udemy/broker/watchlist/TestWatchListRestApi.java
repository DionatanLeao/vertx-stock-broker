package com.udemy.broker.watchlist;

import com.udemy.broker.MainVerticle;
import com.udemy.broker.assets.Asset;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestWatchListRestApi {
  private static final Logger LOG = LoggerFactory.getLogger(TestWatchListRestApi.class);

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void adds_and_returns_watchlist_for_account(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(MainVerticle.PORT));
    var accountId = UUID.randomUUID();
    client.get("/account/watchlist" + accountId)
      .sendJsonObject(body())
      .onComplete(testContext.succeeding(response -> {
        JsonObject json = response.bodyAsJsonObject();
        LOG.info("Response: {}", json);
        assertEquals("", json.encode());
        assertEquals(200, response.statusCode());
        testContext.completeNow();
      }));
  }

  private static JsonObject body() {
    return new WatchList(Arrays.asList(
      new Asset("AMZN"),
      new Asset("TSLA")))
      .toJsonObject();
  }

}
