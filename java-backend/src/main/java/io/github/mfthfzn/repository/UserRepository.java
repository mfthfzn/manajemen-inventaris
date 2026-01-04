package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;

import java.util.Optional;

public interface UserRepository {

  Optional<User> findUserByEmail(String email);

}
