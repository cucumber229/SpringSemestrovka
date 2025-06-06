package itis.semestrovka.demo.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Global exception handler that returns JSON for REST controllers and
 * displays a custom error page for MVC controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api");
    }

    private Object buildResponse(HttpStatusCode status, String message, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse body = new ErrorResponse(status.value(), message);
            return ResponseEntity.status(status.value()).body(body);
        }
        ModelAndView mav = new ModelAndView("error");
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        if (httpStatus != null) {
            mav.setStatus(httpStatus);
        }
        mav.addObject("error", message);
        return mav;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Object handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        return buildResponse(ex.getStatusCode(), ex.getReason() != null ? ex.getReason() : ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public Object handleOtherExceptions(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }
}