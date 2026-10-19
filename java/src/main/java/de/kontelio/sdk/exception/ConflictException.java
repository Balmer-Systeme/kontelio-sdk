package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/** 409 – conflicts with the current state, e.g. a duplicate external reference or an issued (immutable) invoice. */
public class ConflictException extends ApiException {
  public ConflictException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }
}
