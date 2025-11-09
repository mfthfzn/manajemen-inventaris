package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.User;

public interface LoginRepository {

  User findUserByEmail(String email);

}
