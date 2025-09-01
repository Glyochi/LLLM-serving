package com.glygateway.controller;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import com.glygateway.exception.triton.InferenceFailedException;
import com.glygateway.exception.triton.ValidationException;

@RestControllerAdvice
public class GlobalErrorHandler {

  // Problem type URIs you own (serve docs at these paths or via static content)
  private static final URI TYPE_PLACE_HOLDER = URI.create("https://api.example.com/problems/placehoder");
  private static final URI TYPE_NOT_FOUND = URI.create("https://api.example.com/problems/not-found");
  private static final URI TYPE_INFERENCE_FAILED = URI.create("https://api.example.com/problems/not-found");

  private static final URI TYPE_VALIDATION = URI.create("https://api.example.com/problems/validation");
  private static final URI TYPE_BAD_REQUEST = URI.create("https://api.example.com/problems/bad-request");

  // ****************************************************************************************
  // Make Controller Validation RFC 9457 compliant
  // ****************************************************************************************

  // A) Bean Validation on @RequestBody (e.g., @NotBlank) -> 422 + errors[]
  @ExceptionHandler(org.springframework.web.bind.support.WebExchangeBindException.class)
  public ProblemDetail handleBind(org.springframework.web.bind.support.WebExchangeBindException ex,
      org.springframework.web.server.ServerWebExchange exchange) {
    var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    pd.setType(TYPE_VALIDATION);
    pd.setTitle("Request is not valid");
    pd.setDetail("One or more fields are invalid");
    var errors = ex.getFieldErrors().stream()
      .map(fe -> Map.of("detail", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
            "pointer", "#/" + fe.getField()))
      .toList();

    // var debug_str = "";
    // debug_str += "-----------------------------------1 \n";
    // debug_str += ex.toString() + "\n";
    // debug_str += "----------------------------------- \n";
    // debug_str += ex.getFieldErrors() + "\n";
    // debug_str += "----------------------------------- \n";
    // debug_str += errors.toString() + "\n";
    // pd.setProperty("debug", debug_str);

    pd.setInstance(exchange.getRequest().getURI());
    pd.setProperty("errors", errors);
    return pd;
  }

  // B) Malformed JSON / enum mismatch / type errors -> 400
  @ExceptionHandler(org.springframework.web.server.ServerWebInputException.class)
  public ProblemDetail handleInput(org.springframework.web.server.ServerWebInputException ex,
      org.springframework.web.server.ServerWebExchange exchange) {
    var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setType(TYPE_BAD_REQUEST);
    pd.setTitle("Malformed request");
    pd.setDetail(Optional.ofNullable(ex.getReason()).orElse("Could not read request"));
    pd.setInstance(exchange.getRequest().getURI());

    return pd;
  }

  // C) @Validated method/parameter violations -> 422 + errors[]
  // Does this even get here? Need read more
  @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
  public ProblemDetail handleConstraint(jakarta.validation.ConstraintViolationException ex,
      org.springframework.web.server.ServerWebExchange exchange) {
    var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    pd.setType(TYPE_VALIDATION);
    pd.setTitle("Request is not valid");
    pd.setDetail("One or more parameters are invalid");
    pd.setInstance(exchange.getRequest().getURI());
    var errors = ex.getConstraintViolations().stream()
        .map(v -> Map.of("detail", v.getMessage(),
            "pointer", "#/" + v.getPropertyPath().toString()))
        .toList();
    pd.setProperty("errors", errors);

    return pd;
  }

  // ****************************************************************************************
  // END OF Make Controller Validation RFC 9457 compliant
  // ****************************************************************************************


  // ****************************************************************************************
  // Other custom exceptions
  // ****************************************************************************************
  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException ex, ServerWebExchange exchange) {
    var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    pd.setType(TYPE_NOT_FOUND);
    pd.setTitle("Resource not found");
    pd.setDetail(ex.getMessage());
    attachCommon(pd, exchange);
    return pd;
  }

  @ExceptionHandler(InferenceFailedException.class)
  public ProblemDetail handleInferenceFailed(InferenceFailedException ex, ServerWebExchange exchange) {
    var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setType(TYPE_INFERENCE_FAILED);
    pd.setTitle("Inference failed");
    pd.setDetail(ex.getMessage());
    attachCommon(pd, exchange);
    return pd;
  }

  @ExceptionHandler(ValidationException.class)
  public ProblemDetail handleValidation(ValidationException ex, ServerWebExchange exchange) {
    var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setType(TYPE_PLACE_HOLDER);
    pd.setTitle("Invalid request");
    pd.setDetail(ex.getMessage());
    attachCommon(pd, exchange);
    return pd;
  }
  // ****************************************************************************************
  // END OF Other custom exceptions
  // ****************************************************************************************

  private static void attachCommon(ProblemDetail pd, ServerWebExchange exchange) {
    // RFC fields
    pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
    // Extensions (Spring will render these as top-level fields)
    var cid = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
    if (cid != null)
      pd.setProperty("correlationId", cid);
  }
}
