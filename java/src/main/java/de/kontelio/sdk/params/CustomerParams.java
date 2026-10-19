package de.kontelio.sdk.params;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.kontelio.sdk.model.Address;
import de.kontelio.sdk.model.CustomerType;
import de.kontelio.sdk.model.DocumentLanguage;
import de.kontelio.sdk.model.EInvoiceFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data to create or update a customer. Unset fields are not sent; on update only the given
 * fields are changed.
 *
 * <pre>{@code
 * new CustomerParams()
 *     .name("Example Customer GmbH")
 *     .vatId("DE123456789")
 *     .billingAddress(Address.of("Friedrichstraße 1", "10117", "Berlin", "DE"))
 *     .externalReference("CRM-4711");
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class CustomerParams {
  private CustomerType type;
  private String name;
  private String email;
  private String phone;
  private String vatId;
  private String taxNumber;
  private String leitwegId;
  private Address billingAddress;
  private DocumentLanguage language;
  private Integer paymentTermsDays;
  private EInvoiceFormat eInvoiceFormat;
  private String externalReference;
  private Map<String, String> metadata;

  public CustomerParams type(CustomerType type) { this.type = type; return this; }
  public CustomerParams name(String name) { this.name = name; return this; }
  public CustomerParams email(String email) { this.email = email; return this; }
  public CustomerParams phone(String phone) { this.phone = phone; return this; }
  /** EU VAT identification number (USt-IdNr.), e.g. {@code DE123456789}. */
  public CustomerParams vatId(String vatId) { this.vatId = vatId; return this; }
  /** German tax number (Steuernummer). */
  public CustomerParams taxNumber(String taxNumber) { this.taxNumber = taxNumber; return this; }
  /** Leitweg-ID, required for XRechnung invoices to German public authorities. */
  public CustomerParams leitwegId(String leitwegId) { this.leitwegId = leitwegId; return this; }
  public CustomerParams billingAddress(Address billingAddress) { this.billingAddress = billingAddress; return this; }
  public CustomerParams language(DocumentLanguage language) { this.language = language; return this; }
  public CustomerParams paymentTermsDays(int paymentTermsDays) { this.paymentTermsDays = paymentTermsDays; return this; }
  public CustomerParams eInvoiceFormat(EInvoiceFormat eInvoiceFormat) { this.eInvoiceFormat = eInvoiceFormat; return this; }
  /** Your own identifier for this customer (e.g. ERP/CRM ID). Unique per account. */
  public CustomerParams externalReference(String externalReference) { this.externalReference = externalReference; return this; }

  public CustomerParams metadata(String key, String value) {
    if (metadata == null) metadata = new LinkedHashMap<>();
    metadata.put(key, value);
    return this;
  }
}
