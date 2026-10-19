package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/** 403 – the API key lacks the required scope. */
public class PermissionDeniedException extends ApiException {
  public PermissionDeniedException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }
}
