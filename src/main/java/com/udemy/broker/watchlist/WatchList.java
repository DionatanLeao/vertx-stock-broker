package com.udemy.broker.watchlist;

import com.udemy.broker.assets.Asset;
import io.vertx.core.json.JsonObject;
import lombok.Value;

import java.util.List;

@Value
public class WatchList {
  private List<Asset> assets;

  JsonObject toJsonObject() {
    return JsonObject.mapFrom(this);
  }
}
