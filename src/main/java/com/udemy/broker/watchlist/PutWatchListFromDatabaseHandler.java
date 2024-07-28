package com.udemy.broker.watchlist;

import com.udemy.broker.db.migration.DbResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PutWatchListFromDatabaseHandler implements Handler<RoutingContext> {
  private static final Logger LOG = LoggerFactory.getLogger(PutWatchListFromDatabaseHandler.class);
  private final Pool db;

  public PutWatchListFromDatabaseHandler(Pool db) {
    this.db = db;
  }

  @Override
  public void handle(RoutingContext context) {
    String accountId = WatchListRestApi.getAccountId(context);
    JsonObject json = context.getBodyAsJson();
    WatchList watchList = json.mapTo(WatchList.class);

    List<Map<String, Object>> parameterBatch = watchList.getAssets().stream().map(asset -> {
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("account_id", accountId);
      parameters.put("asset", asset.getName());
      return parameters;
    }).collect(Collectors.toList());

    // Only adding is possible -> Entries for watch list are never removed
    SqlTemplate.forUpdate(db,
      "INSERT INTO broker.watchlist VALUES (#{account_id},#{asset})" +
        " ON CONFLICT (account_id, asset) DO NOTHING")
      .executeBatch(parameterBatch)
      .onFailure(DbResponse.errorHandler(context, "Failed to insert into watchlist"))
      .onSuccess(result -> {
        if (!context.response().ended()) {
          context.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
            .end();
        }
      });
  }
}
