package com.udemy.broker.quotes;

import com.udemy.broker.assets.GetAssetsHandler;
import com.udemy.broker.db.DbResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class GetQuoteHandler implements Handler<RoutingContext> {
  private static final Logger LOG = LoggerFactory.getLogger(GetQuoteHandler.class);
  private final Map<String, Quote> cachedQuotes;

  public GetQuoteHandler(final Map<String, Quote> cachedQuotes) {
    this.cachedQuotes = cachedQuotes;
  }

  @Override
  public void handle(RoutingContext context) {
    final String assetParam = context.pathParam("asset");
    LOG.debug("Asset parameter: {}", assetParam);

    var maybeQuotes = Optional.ofNullable(cachedQuotes.get(assetParam));
    if(maybeQuotes.isEmpty()) {
      DbResponse.notFound(context, "quote for asset " + assetParam + " not available!");
      return;
    }

    final JsonObject response = maybeQuotes.get().toJsonObject();
    LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
    context.response().end(response.toBuffer());
  }
}
