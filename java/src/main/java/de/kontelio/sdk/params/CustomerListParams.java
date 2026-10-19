package de.kontelio.sdk.params;

/** Filters for listing customers. */
public final class CustomerListParams extends ListParams<CustomerListParams> {

  /** Full-text search over name, number, email and VAT ID. */
  public CustomerListParams search(String search) { return set("search", search); }
  public CustomerListParams externalReference(String externalReference) { return set("externalReference", externalReference); }
  public CustomerListParams email(String email) { return set("email", email); }
}
