package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The customer data embedded in an {@link Invoice}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerSummary(String id, String number, String name, String vatId, Address billingAddress) {}
