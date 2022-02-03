package tech.returnzero.microexception.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import tech.returnzero.microexception.MicroException;
import tech.returnzero.microexception.response.ErrorResponse;

@ControllerAdvice
public class MicroExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String BAD_REQUEST = "BAD_REQUEST";

    @ExceptionHandler(MicroException.class)
    public final ResponseEntity<ErrorResponse> handleMicroException(MicroException exception) {
        List<String> details = new ArrayList<>();
        details.add(exception.getMessage());
        ErrorResponse error = new ErrorResponse(BAD_REQUEST, details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
