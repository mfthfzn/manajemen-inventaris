package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.repository.LoginRepositoryImpl;

public class LoginServiceImpl implements LoginService{

  private LoginRepositoryImpl loginRepository;

  public LoginServiceImpl(LoginRepositoryImpl loginRepository) {
    this.loginRepository = loginRepository;
  }

  @Override
  public boolean isEmailRegistered(LoginRequest loginRequest) {
    User user = loginRepository.findUserByEmail(loginRequest.getEmail());
    return user != null;
  }

  @Override
  public boolean authenticate(LoginRequest loginRequest) {
    User user = loginRepository.findUserByEmail(loginRequest.getEmail());

    return loginRequest.getEmail().equals(user.getEmail()) && loginRequest.getPassword().equals(user.getPassword());
  }
}
