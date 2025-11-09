package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.dto.LoginResponse;
import io.github.mfthfzn.entity.User;

public interface LoginService {

  boolean isEmailRegistered(LoginRequest loginRequest);

  boolean authenticate(LoginRequest loginRequest);

}
