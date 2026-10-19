package de.kontelio.sdk;

import static de.kontelio.sdk.internal.HttpTransport.encodePath;

import com.fasterxml.jackson.databind.JavaType;
import de.kontelio.sdk.internal.HttpTransport;
import de.kontelio.sdk.internal.Json;
import de.kontelio.sdk.model.Customer;
import de.kontelio.sdk.model.DeletedObject;
import de.kontelio.sdk.params.CustomerListParams;
import de.kontelio.sdk.params.CustomerParams;
import java.util.List;
import java.util.Map;

/** Customers (Debitoren). */
public final class CustomerService {
  private static final JavaType CUSTOMER = Json.MAPPER.constructType(Customer.class);
  private static final JavaType DELETED = Json.MAPPER.constructType(DeletedObject.class);
  private static final JavaType CUSTOMER_LIST = Json.MAPPER.getTypeFactory().constructParametricType(Page.Envelope.class, Customer.class);

  private final HttpTransport http;

  CustomerService(HttpTransport http) {
    this.http = http;
  }

  public Customer create(CustomerParams params) {
    return create(params, null);
  }

  public Customer create(CustomerParams params, RequestOptions options) {
    return http.json("POST", "/customers", null, params, CUSTOMER, options);
  }

  public Customer retrieve(String id) {
    return retrieve(id, null);
  }

  public Customer retrieve(String id, RequestOptions options) {
    return http.json("GET", "/customers/" + encodePath(id), null, null, CUSTOMER, options);
  }

  /** Updates a customer. Only the given fields are changed. */
  public Customer update(String id, CustomerParams params) {
    return update(id, params, null);
  }

  public Customer update(String id, CustomerParams params, RequestOptions options) {
    return http.json("PATCH", "/customers/" + encodePath(id), null, params, CUSTOMER, options);
  }

  public Page<Customer> list() {
    return list(new CustomerListParams(), null);
  }

  public Page<Customer> list(CustomerListParams params) {
    return list(params, null);
  }

  public Page<Customer> list(CustomerListParams params, RequestOptions options) {
    return fetchPage(params.toQuery(), params, options);
  }

  private Page<Customer> fetchPage(Map<String, List<String>> query, CustomerListParams params, RequestOptions options) {
    Page.Envelope<Customer> envelope = http.json("GET", "/customers", query, null, CUSTOMER_LIST, options);
    return new Page<>(envelope, cursor -> fetchPage(params.toQuery(cursor), params, options));
  }

  /**
   * Deletes a customer. Customers referenced by issued invoices are archived instead (retention
   * obligations, § 147 AO).
   */
  public DeletedObject delete(String id) {
    return delete(id, null);
  }

  public DeletedObject delete(String id, RequestOptions options) {
    return http.json("DELETE", "/customers/" + encodePath(id), null, null, DELETED, options);
  }
}
