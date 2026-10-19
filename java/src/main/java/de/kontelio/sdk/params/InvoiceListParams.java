package de.kontelio.sdk.params;

import de.kontelio.sdk.model.InvoiceStatus;
import java.time.LocalDate;

/** Filters for listing invoices. */
public final class InvoiceListParams extends ListParams<InvoiceListParams> {

  /** Filter by one or more statuses. */
  public InvoiceListParams status(InvoiceStatus... statuses) {
    set("status", null);
    for (InvoiceStatus status : statuses) add("status", status.value());
    return this;
  }

  public InvoiceListParams customerId(String customerId) { return set("customerId", customerId); }
  public InvoiceListParams externalReference(String externalReference) { return set("externalReference", externalReference); }
  /** Issue date lower bound (inclusive). */
  public InvoiceListParams issueDateFrom(LocalDate from) { return set("issueDateFrom", from); }
  /** Issue date upper bound (inclusive). */
  public InvoiceListParams issueDateTo(LocalDate to) { return set("issueDateTo", to); }
  public InvoiceListParams search(String search) { return set("search", search); }
}
