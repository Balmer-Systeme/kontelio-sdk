package de.kontelio.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Confirmation of a deletion. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DeletedObject(String id, String object, boolean deleted) {}
