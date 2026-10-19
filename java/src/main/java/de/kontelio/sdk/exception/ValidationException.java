package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/** 422 – the request failed validation; see {@link #details()}. */
public class ValidationException extends ApiException {
  public ValidationException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }
}
