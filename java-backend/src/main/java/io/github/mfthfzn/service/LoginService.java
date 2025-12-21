package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.dto.LoginResponse;
import io.github.mfthfzn.entity.User;

public interface LoginService {

  boolean authenticate(LoginRequest loginRequest);

  LoginResponse getUser(LoginRequest loginRequest);

  String generateToken(LoginRequest loginRequest);

}
