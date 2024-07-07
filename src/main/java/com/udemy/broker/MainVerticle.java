package com.udemy.broker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);
  public static final int PORT = 8888;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.exceptionHandler(error -> {
      LOG.error("Unhandled:", error);
    });
    vertx.deployVerticle(new MainVerticle())
      .onFailure(err  -> LOG.error("Failed to deploy:", err))
      .onSuccess(id ->
        LOG.info("Deployed {} with id {}", MainVerticle.class.getSimpleName(), id)
      );
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(RestApiVerticle.class.getName(),
        new DeploymentOptions().setInstances(processors()))
      .onFailure(startPromise::fail)
      .onSuccess(id -> {
        LOG.info("Deployed {} with id {}", RestApiVerticle.class.getSimpleName(), id);
        startPromise.complete();
      });
  }

  private static int processors() {
    return Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
  }

}
