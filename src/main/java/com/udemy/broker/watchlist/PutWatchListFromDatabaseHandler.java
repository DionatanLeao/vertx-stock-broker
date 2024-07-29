package com.udemy.broker.watchlist;

import com.udemy.broker.db.migration.DbResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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

    // Transaction
    db.withTransaction(client -> {
      // 1 - Delete all for account_id
      return SqlTemplate.forUpdate(client,
        "DELETE FROM broker.watchlist w WHERE w.account_id = #{account_id}")
        .execute(Collections.singletonMap("account_id", accountId))
        .onFailure(DbResponse.errorHandler(context, "Failed to clear watchlist for accountId: " + accountId))
        .compose(deletionDone -> {
          // 2 - Add all for account_id
          return addAllForAccountId(client, context, parameterBatch);
        })
        .onFailure(DbResponse.errorHandler(context, "Failed to update watchlist for accountId: " + accountId))
        .onSuccess(result -> {
          // 3 - Both succeeded
          context.response()
            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
            .end();
        });
    });
  }

  private Future<SqlResult<Void>> addAllForAccountId(SqlConnection client, RoutingContext context, List<Map<String, Object>> parameterBatch) {
    return SqlTemplate.forUpdate(client,
        "INSERT INTO broker.watchlist VALUES (#{account_id},#{asset})" +
          " ON CONFLICT (account_id, asset) DO NOTHING")
      .executeBatch(parameterBatch)
      .onFailure(DbResponse.errorHandler(context, "Failed to insert into watchlist"));
  }
}
