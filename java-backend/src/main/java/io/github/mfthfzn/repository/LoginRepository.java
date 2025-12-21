package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;

public interface LoginRepository {

  User findUserByEmail(String email);

  boolean setTokenSession(String email, String token);

  TokenSession findTokenByEmail(String email);

}
