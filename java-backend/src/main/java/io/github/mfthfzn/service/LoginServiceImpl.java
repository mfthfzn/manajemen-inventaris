package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.repository.LoginRepositoryImpl;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

import java.util.Optional;

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
  public User getUserByEmail(LoginRequest loginRequest) {
    Optional<User> userOptional = Optional.ofNullable(loginRepository.findUserByEmail(loginRequest.getEmail()));
    if (userOptional.isPresent()) {
      return userOptional.get();
    } else {
      return null;
    }
  }
}
