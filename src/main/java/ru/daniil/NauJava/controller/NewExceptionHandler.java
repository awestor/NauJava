package ru.daniil.NauJava.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.daniil.NauJava.exception.*;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class NewExceptionHandler {
    /**
     * Данный метод обрабатывает ошибку о не найденном ресурсе (странице или данных), а после
     * создаёт объект класса ApiError и возвращает страницу с ошибкой
     *
     * @param ex      ошибка
     * @param request запрос в котором произошла ошибка
     * @return страница с ошибкой
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                getRequestPath(request)
        );

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Данный метод обрабатывает ошибку невалидных данных в одном поле, а после
     * создаёт объект класса ApiError и возвращает страницу с ошибкой
     *
     * @param ex      ошибка
     * @param request запрос в котором произошла ошибка
     * @return страница с ошибкой
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(ValidationException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                getRequestPath(request)
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Данный метод обрабатывает ошибку невалидных данных во множестве полей, а после
     * создаёт объект класса ApiError и возвращает страницу с ошибкой
     *
     * @param ex      ошибка
     * @param request запрос в котором произошла ошибка
     * @return страница с ошибкой
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        List<ApiError.ValidationError> validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    Object rejectedValue = ((FieldError) error).getRejectedValue();
                    return new ApiError.ValidationError(fieldName, errorMessage, rejectedValue);
                })
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(
                "Validation failed for one or more fields",
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                getRequestPath(request)
        );
        apiError.setValidationErrors(validationErrors);

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                getRequestPath(request)
        );

        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                "Доступ запрещен",
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                getRequestPath(request)
        );

        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
            return servletRequest.getRequestURI();
        }
        return "Unknown";
    }
}
