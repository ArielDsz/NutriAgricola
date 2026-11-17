package com.nutriagricola.usuarios.exception;

public class ResourceAlreadyExistsException extends RuntimeException{
    public ResourceAlreadyExistsException(String message){
        super(message);
    }
}
