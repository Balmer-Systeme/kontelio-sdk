package de.kontelio.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * One page of a list result.
 *
 * <p>Use {@link #autoPagingIterable()} or {@link #autoPagingStream()} to walk through all
 * following pages lazily:
 *
 * <pre>{@code
 * for (Invoice invoice : kontelio.invoices().list(params).autoPagingIterable()) {
 *   System.out.println(invoice.number());
 * }
 * }</pre>
 *
 * @param <T> item type
 */
public final class Page<T> {
  private final List<T> data;
  private final boolean hasMore;
  private final String nextCursor;
  private final Function<String, Page<T>> fetchNext;

  /** @hidden */
  public Page(Envelope<T> envelope, Function<String, Page<T>> fetchNext) {
    this.data = envelope.data() == null ? List.of() : List.copyOf(envelope.data());
    this.hasMore = envelope.hasMore();
    this.nextCursor = envelope.nextCursor();
    this.fetchNext = fetchNext;
  }

  /** Items of this page. */
  public List<T> data() { return data; }
  public boolean hasMore() { return hasMore; }
  public String nextCursor() { return nextCursor; }

  public boolean hasNextPage() {
    return hasMore && nextCursor != null;
  }

  /** Fetches the next page, or returns empty on the last page. */
  public Optional<Page<T>> nextPage() {
    return hasNextPage() ? Optional.of(fetchNext.apply(nextCursor)) : Optional.empty();
  }

  /** Iterates over all items of this and all following pages. */
  public Iterable<T> autoPagingIterable() {
    return () -> new Iterator<>() {
      private Page<T> page = Page.this;
      private int index = 0;

      @Override
      public boolean hasNext() {
        while (index >= page.data.size()) {
          if (!page.hasNextPage()) return false;
          page = page.fetchNext.apply(page.nextCursor);
          index = 0;
        }
        return true;
      }

      @Override
      public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        return page.data.get(index++);
      }
    };
  }

  /** Lazy stream over all items of this and all following pages. */
  public Stream<T> autoPagingStream() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(autoPagingIterable().iterator(), Spliterator.ORDERED), false);
  }

  /** Collects all items of all pages. Use with care on large lists. */
  public List<T> toList() {
    List<T> all = new ArrayList<>();
    autoPagingIterable().forEach(all::add);
    return all;
  }

  /**
   * Wire format of list responses.
   *
   * @hidden
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Envelope<T>(String object, List<T> data, boolean hasMore, String nextCursor) {}
}
