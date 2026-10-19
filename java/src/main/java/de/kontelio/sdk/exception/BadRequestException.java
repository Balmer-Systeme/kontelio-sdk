package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/** 400 – malformed request. */
public class BadRequestException extends ApiException {
  public BadRequestException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }
}
