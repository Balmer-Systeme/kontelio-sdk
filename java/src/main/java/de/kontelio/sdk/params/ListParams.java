package de.kontelio.sdk.params;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Base class of list filters: page size, cursor and resource-specific query parameters. */
public abstract class ListParams<T extends ListParams<T>> {
  private final Map<String, List<String>> query = new LinkedHashMap<>();

  @SuppressWarnings("unchecked")
  protected T set(String key, Object value) {
    if (value == null) query.remove(key);
    else query.put(key, new ArrayList<>(List.of(String.valueOf(value))));
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  protected T add(String key, Object value) {
    query.computeIfAbsent(key, k -> new ArrayList<>()).add(String.valueOf(value));
    return (T) this;
  }

  /** Items per page (1–100, default 25). */
  public T limit(int limit) {
    return set("limit", limit);
  }

  /** Cursor returned as {@code nextCursor} by the previous page. */
  public T cursor(String cursor) {
    return set("cursor", cursor);
  }

  /** The query parameters as sent to the API. */
  public Map<String, List<String>> toQuery() {
    Map<String, List<String>> copy = new LinkedHashMap<>();
    query.forEach((k, v) -> copy.put(k, List.copyOf(v)));
    return copy;
  }

  /** A copy of these params for the page after {@code cursor}. */
  public Map<String, List<String>> toQuery(String cursor) {
    Map<String, List<String>> copy = toQuery();
    copy.put("cursor", List.of(cursor));
    return copy;
  }
}
