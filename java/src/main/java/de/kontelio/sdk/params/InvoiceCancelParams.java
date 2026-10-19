package de.kontelio.sdk.params;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

/** Options for cancelling an invoice (Stornorechnung). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class InvoiceCancelParams {
  private String reason;
  private LocalDate cancellationDate;

  /** Reason printed on the cancellation invoice. */
  public InvoiceCancelParams reason(String reason) { this.reason = reason; return this; }
  /** Defaults to today. */
  public InvoiceCancelParams cancellationDate(LocalDate cancellationDate) { this.cancellationDate = cancellationDate; return this; }
}
