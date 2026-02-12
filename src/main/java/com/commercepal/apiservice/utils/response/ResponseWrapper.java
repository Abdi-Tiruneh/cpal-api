package com.commercepal.apiservice.utils.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
public class ResponseWrapper<T> {

  private final int status;
  private final String message;
  private final T data;

  // Success response with message and data
  public ResponseWrapper(String message, T data) {
    this.status = 200;
    this.message = message;
    this.data = data;
  }

  // Success response with data only
  public ResponseWrapper(T data) {
    this.status = 200;
    this.message = "Success";
    this.data = data;
  }

  // Success response with message only
  public ResponseWrapper(String message) {
    this.status = 200;
    this.message = message;
    this.data = null;
  }

  // Static methods for success responses
  public static <T> ResponseEntity<ResponseWrapper<T>> success(String message, T data) {
    return ResponseEntity.ok(new ResponseWrapper<>(message, data));
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> success(T data) {
    return ResponseEntity.ok(new ResponseWrapper<>(data));
  }

  public static <T> ResponseEntity<ResponseWrapper<PagedResponse<T>>> success(Page<T> page) {
    return ResponseWrapper.success(PagedResponse.from(page));
  }

  public static <T> ResponseEntity<ResponseWrapper<PagedResponse<T>>> success(String message,
      Page<T> page) {
    return ResponseWrapper.success(message, PagedResponse.from(page));
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> success(String message) {
    return ResponseEntity.ok(new ResponseWrapper<>(message));
  }

  public static <T> ResponseEntity<ResponseWrapper<ProductPagedResponse<T>>> successProducts(
      int page, int size, List<T> items) {
    return ResponseEntity.ok(
        new ResponseWrapper<>(ProductPagedResponse.from(page, size, items)));
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> created(T data) {
    return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseWrapper<>(data));
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> created(String message, T data) {
    return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseWrapper<>(message, data));
  }

  // Static methods for error responses
  public static <T> ResponseEntity<ResponseWrapper<T>> error(int status, String message) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ResponseWrapper<>(status, message, null));
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> error(HttpStatus httpStatus,
      String message) {
    return ResponseEntity.status(httpStatus)
        .body(new ResponseWrapper<>(httpStatus.value(), message, null));
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> error(HttpStatus httpStatus,
      String message, T data) {
    return ResponseEntity.status(httpStatus)
        .body(new ResponseWrapper<>(httpStatus.value(), message, null));
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> badRequest(int status, String message) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ResponseWrapper<>(status, message, null));
  }


  public static <T> ResponseEntity<ResponseWrapper<T>> error(int status, String message,
      T data) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ResponseWrapper<>(status, message, data));
  }


  // Static methods for common HTTP status statuss
  public static <T> ResponseEntity<ResponseWrapper<T>> badRequest(String message) {
    return error(400, message);
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> unauthorized(String message) {
    return error(401, message);
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> forbidden(String message) {
    return error(403, message);
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> notFound(String message) {
    return error(404, message);
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> conflict(String message) {
    return error(409, message);
  }

  public static <T> ResponseEntity<ResponseWrapper<T>> internalServerError(String message) {
    return error(500, message);
  }

  // Additional methods for security system
  public static <T> ResponseWrapper<T> error(String message, String code) {
    return new ResponseWrapper<>(400, message + " (" + code + ")", null);
  }
}