package de.kontelio.sdk.params;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.kontelio.sdk.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** A payment received for an invoice. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class PaymentParams {
  private final BigDecimal amount;
  private LocalDate date;
  private PaymentMethod method;
  private String reference;

  public PaymentParams(BigDecimal amount) {
    this.amount = Objects.requireNonNull(amount, "amount");
  }

  public static PaymentParams of(String amount) {
    return new PaymentParams(new BigDecimal(amount));
  }

  /** Defaults to today. */
  public PaymentParams date(LocalDate date) { this.date = date; return this; }
  public PaymentParams method(PaymentMethod method) { this.method = method; return this; }
  /** E.g. the bank transaction ID or payment provider reference. */
  public PaymentParams reference(String reference) { this.reference = reference; return this; }
}
