package com.gymmanager.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " no encontrado con " + field + ": " + value, HttpStatus.NOT_FOUND);
    }
}