package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/** 404 – the requested object does not exist. */
public class NotFoundException extends ApiException {
  public NotFoundException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }
}
