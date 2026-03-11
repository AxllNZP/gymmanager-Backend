package com.gymmanager.service;

import com.gymmanager.dto.LoginRequest;
import com.gymmanager.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}