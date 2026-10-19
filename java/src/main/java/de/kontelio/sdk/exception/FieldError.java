package de.kontelio.sdk.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A field-level problem reported with a validation error.
 *
 * @param field dotted path of the offending field, e.g. {@code lines[0].unitPrice}
 * @param code machine-readable code
 * @param message human-readable message
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FieldError(String field, String code, String message) {}
