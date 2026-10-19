package de.kontelio.sdk.params;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.kontelio.sdk.model.ServicePeriod;
import de.kontelio.sdk.model.TaxTreatment;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A line item of an invoice.
 *
 * <pre>{@code
 * InvoiceLineParams.of("Software Development", "95.00").quantity(12).unit("HUR").vatRate(19)
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class InvoiceLineParams {
  private final String name;
  private final BigDecimal unitPrice;
  private String description;
  private String sku;
  private BigDecimal quantity;
  private String unit;
  private BigDecimal vatRate;
  private TaxTreatment taxTreatment;
  private BigDecimal discountPercent;
  private BigDecimal discountAmount;
  private ServicePeriod servicePeriod;
  private String externalReference;

  /**
   * @param name line title shown on the invoice
   * @param unitPrice unit price, net or gross depending on the invoice price mode
   */
  public InvoiceLineParams(String name, BigDecimal unitPrice) {
    this.name = Objects.requireNonNull(name, "name");
    this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
  }

  public static InvoiceLineParams of(String name, BigDecimal unitPrice) {
    return new InvoiceLineParams(name, unitPrice);
  }

  /** Convenience overload taking the price as decimal string, e.g. {@code "95.00"}. */
  public static InvoiceLineParams of(String name, String unitPrice) {
    return new InvoiceLineParams(name, new BigDecimal(unitPrice));
  }

  public InvoiceLineParams description(String description) { this.description = description; return this; }
  public InvoiceLineParams sku(String sku) { this.sku = sku; return this; }
  /** Defaults to 1. */
  public InvoiceLineParams quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
  public InvoiceLineParams quantity(long quantity) { return quantity(BigDecimal.valueOf(quantity)); }
  /** UN/ECE Rec. 20 unit code: {@code C62} piece (default), {@code HUR} hour, {@code DAY}, {@code MON}. */
  public InvoiceLineParams unit(String unit) { this.unit = unit; return this; }
  /** VAT rate in percent, e.g. 19, 7 or 0. */
  public InvoiceLineParams vatRate(BigDecimal vatRate) { this.vatRate = vatRate; return this; }
  public InvoiceLineParams vatRate(int vatRate) { return vatRate(BigDecimal.valueOf(vatRate)); }
  /** Overrides the invoice-level tax treatment for this line. */
  public InvoiceLineParams taxTreatment(TaxTreatment taxTreatment) { this.taxTreatment = taxTreatment; return this; }
  public InvoiceLineParams discountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; return this; }
  public InvoiceLineParams discountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
  public InvoiceLineParams servicePeriod(ServicePeriod servicePeriod) { this.servicePeriod = servicePeriod; return this; }
  public InvoiceLineParams externalReference(String externalReference) { this.externalReference = externalReference; return this; }
}
