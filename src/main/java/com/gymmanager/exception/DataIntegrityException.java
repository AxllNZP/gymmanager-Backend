package com.gymmanager.exception;

import org.springframework.http.HttpStatus;

public class DataIntegrityException extends BaseException {

    public DataIntegrityException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}