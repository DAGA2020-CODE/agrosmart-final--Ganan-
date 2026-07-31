package ec.edu.espe.agrosmart.controller;

import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleProductoNoEncontrado(ProductoNoEncontradoException ex) {
        return ex.getMessage();
    }
}