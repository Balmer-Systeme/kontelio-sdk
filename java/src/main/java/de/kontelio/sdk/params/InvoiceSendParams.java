package de.kontelio.sdk.params;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Options for sending an invoice by email. All fields are optional. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class InvoiceSendParams {
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;
  private String subject;
  private String message;
  private Boolean attachEInvoice;

  /** Recipients. Defaults to the customer's email. */
  public InvoiceSendParams to(String... to) { this.to = List.of(to); return this; }
  public InvoiceSendParams cc(String... cc) { this.cc = List.of(cc); return this; }
  public InvoiceSendParams bcc(String... bcc) { this.bcc = List.of(bcc); return this; }
  public InvoiceSendParams subject(String subject) { this.subject = subject; return this; }
  public InvoiceSendParams message(String message) { this.message = message; return this; }
  /** Attach the e-invoice XML in addition to the PDF. Defaults to {@code true}. */
  public InvoiceSendParams attachEInvoice(boolean attachEInvoice) { this.attachEInvoice = attachEInvoice; return this; }
}
