package com.utn.space.venueaapi.exceptions;

public class ExceptionIdNotFound extends RuntimeException {
    public ExceptionIdNotFound(String objet,Long id) {
        super("El id: " + id + " de " + objet + " no existe.");
    }
}
