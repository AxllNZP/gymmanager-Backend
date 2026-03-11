package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.dto.LoginRequest;
import com.gymmanager.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}