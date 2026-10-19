# German VAT treatments (Umsatzsteuer)

Set `taxTreatment` on the invoice (or per line) so that Kontelio prints the
legally required notes, computes VAT correctly and books the revenue to the
right fields of your UStVA and EÜR.

| `taxTreatment` | When | VAT | Note printed on the invoice |
| --- | --- | --- | --- |
| `standard` | Domestic supply | 19 % / 7 % (`vatRate`) | – |
| `small_business` | Kleinunternehmer, § 19 UStG | 0 % | "Gemäß § 19 UStG wird keine Umsatzsteuer berechnet." |
| `reverse_charge` | B2B service to another EU country, § 13b UStG | 0 % | "Steuerschuldnerschaft des Leistungsempfängers" |
| `intra_community_supply` | Goods to a business in another EU country, § 4 Nr. 1b UStG | 0 % | "Steuerfreie innergemeinschaftliche Lieferung" |
| `export` | Goods outside the EU, § 4 Nr. 1a UStG | 0 % | "Steuerfreie Ausfuhrlieferung" |
| `exempt` | Other exempt supplies (e.g. § 4 Nr. 14 UStG) | 0 % | Exemption reason |
| `not_taxable_in_germany` | Place of supply abroad (e.g. B2C services outside the EU) | 0 % | "Nicht im Inland steuerbar" |

For `reverse_charge` and `intra_community_supply` the customer's **VAT ID**
(`vatId`) is required; it is also reported in the Zusammenfassende Meldung.

```ts
await kontelio.invoices.create({
  customer: { id: "cus_fr_42" }, // has vatId FR12345678901
  taxTreatment: "reverse_charge",
  lines: [{ name: "Consulting", quantity: 8, unit: "HUR", unitPrice: 120 }],
});
```

> This page describes how the API maps tax treatments. It is not tax advice —
> please consult your tax advisor (Steuerberater) for your specific case.
