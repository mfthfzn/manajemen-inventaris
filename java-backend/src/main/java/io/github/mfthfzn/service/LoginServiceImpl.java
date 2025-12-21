package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.dto.LoginResponse;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.repository.LoginRepositoryImpl;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

import java.util.Optional;
import java.util.UUID;

public class LoginServiceImpl implements LoginService{

  private final LoginRepositoryImpl loginRepository;

  public LoginServiceImpl(LoginRepositoryImpl loginRepository) {
    this.loginRepository = loginRepository;
  }

  @Override
  public boolean authenticate(LoginRequest loginRequest) {
    Optional<User> userOptional = Optional.ofNullable(loginRepository.findUserByEmail(loginRequest.getEmail()));
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      return loginRequest.getEmail().equals(user.getEmail()) && loginRequest.getPassword().equals(user.getPassword());
    } else {
      return false;
    }
  }

  @Override
  public LoginResponse getUser(LoginRequest loginRequest) {
    Optional<User> userOptional = Optional.ofNullable(loginRepository.findUserByEmail(loginRequest.getEmail()));
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      LoginResponse loginResponse = new LoginResponse();
      loginResponse.setEmail(user.getEmail());
      loginResponse.setName(user.getName());
      loginResponse.setRole(user.getRole());
      return loginResponse;
    } else {
      return null;
    }
  }

  @Override
  public String generateToken(LoginRequest loginRequest) {
    String token = UUID.randomUUID().toString();
    boolean resultSetToken = loginRepository.setTokenSession(loginRequest.getEmail(), token);
    if (resultSetToken) {
      return token;
    } else {
      return null;
    }
  }
}
