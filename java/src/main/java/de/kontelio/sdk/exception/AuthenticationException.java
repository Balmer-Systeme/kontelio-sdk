package de.kontelio.sdk.exception;

import java.net.http.HttpHeaders;
import java.util.List;

/** 401 – missing or invalid API key. */
public class AuthenticationException extends ApiException {
  public AuthenticationException(int status, String message, String code, String type, String param, List<FieldError> details, HttpHeaders headers, String body) {
    super(status, message, code, type, param, details, headers, body);
  }
}
