package de.kontelio.sdk;

final class Fixtures {
  static final String INVOICE = """
      {
        "id": "inv_01",
        "object": "invoice",
        "number": "RE-2026-0001",
        "status": "issued",
        "customer": { "id": "cus_1", "number": "K-1", "name": "Example GmbH", "vatId": "DE123456789" },
        "issueDate": "2026-10-19",
        "currency": "EUR",
        "eInvoiceFormat": "zugferd",
        "taxTreatment": "standard",
        "lines": [
          { "id": "l1", "position": 1, "name": "Dev", "quantity": "10", "unit": "HUR", "unitPrice": "100.00",
            "vatRate": "19", "netAmount": "1000.00", "vatAmount": "190.00", "grossAmount": "1190.00" }
        ],
        "totals": { "netAmount": "1000.00", "vatAmount": "190.00", "grossAmount": "1190.00",
                    "amountPaid": "0.00", "amountDue": "1190.00",
                    "vatBreakdown": [ { "vatRate": "19", "taxTreatment": "standard", "netAmount": "1000.00", "vatAmount": "190.00" } ] },
        "issuedAt": "2026-10-19T12:00:00Z",
        "createdAt": "2026-10-19T11:59:00Z",
        "updatedAt": "2026-10-19T12:00:00Z",
        "someFutureField": true
      }
      """;

  private Fixtures() {}
}
