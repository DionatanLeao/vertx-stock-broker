package com.udemy.broker.assets;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetsRestApi {
  private static final Logger LOG = LoggerFactory.getLogger(AssetsRestApi.class);
  public static void attach(Router parent) {
    parent.get("/assets").handler(context -> {
      final JsonArray response = new JsonArray();
      response.add(new JsonObject().put("symbol", "AAPL"));
      response.add(new JsonObject().put("symbol", "AMZN"));
      response.add(new JsonObject().put("symbol", "NFLX"));
      response.add(new JsonObject().put("symbol", "TSLA"));
      LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
      context.response().end(response.toBuffer());
    });
  }
}