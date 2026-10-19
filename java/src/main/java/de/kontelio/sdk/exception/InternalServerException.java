package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/** 5xx – something went wrong on Kontelio's side. */
public class InternalServerException extends ApiException {
  public InternalServerException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }
}
